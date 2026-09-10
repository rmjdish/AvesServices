import sys
import os
import re
from os import getenv
import csv
import time
import savReaderWriter as spss
import locale
import fnmatch as fn
import shutil as sh
from _stat import filemode
from pathlib import Path
import pandas as pd
from sqlalchemy.orm import sessionmaker
# import package components
from . commondefs import *
from . dbutils  import DBUtils
from . fileutils import FileOps
from . basketutils import BasketBuilder



class Scramble:

    def __init__(self):
        """! @brief Uses YAML file for initialisation
        @file "/opt/swift/bobthebuilder.yaml"
            YAML properties file (can be shared with Swift Java sources) containing
            database configuration parameters and other settings.
        """
        super().__init__()  # This now initializes the shared state
        self.fops = FileOps()
        self.props = self.get_props()
        self.dbob = DBUtils("scramble")
        self.doChecks()
        if self.preReqs():
            self.info_log(f"{self.ThisVersion()} Scramble: _init_ : Prerequisites satisfied able to scramble")
        else:
            self.error_log(f"{self.ThisVersion()} Scramble: _init_ : Cannot proceed to scramble.")
            quit()
            

    ###############################################################################

    def doChecks(self):
        """! @brief Reads database configuration parameters from a properties file
         and opens connections to MySQL metadata and PostgreSQL data DBs.

        Stores the connections as object local state variables.  In
        addition it checks that an appropriate folder exists in the
        local server file space and creates one if not present.

        """          
        # Connect to metadata database
        try:
            self.metaDB = self.get_scrmdbase()
        except (MetaOpenError, MetaAccessError) as err:
            """ The following is python 3.10+
            match err:
                case MetaOpenError():
                    self.error_log(f"{self.ThisVersion()} Metadata connection failed: {err}")
                    quit()
                case MetaAccessError():
                    self.error_log(f"{self.ThisVersion()} Metadata connection failed: {err}")
                    quit()
                case DBaseError():
                    self.error_log(f"{self.ThisVersion()} Metadata other error: {err}")
                    quit()
                case _:
                    self.error_log(f"{self.ThisVersion()} Unknown error {err}")
                    quit()
            """
            # Python <=3.9
            if isinstance(err, MetaOpenError):
                self.error_log(f"{self.ThisVersion()} Metadata connection failed: {err}")
                quit()
            elif isinstance(err, MetaAccessError):
                self.error_log(f"{self.ThisVersion()} Metadata connection failed: {err}")
                quit()
            elif isinstance(err, DBaseError):
                self.error_log(f"{self.ThisVersion()} Metadata other error: {err}")
                quit()
            else:
                self.error_log(f"{self.ThisVersion()} Unknown error {err}")
                quit()                
        self.info_log(f"{self.ThisVersion()} Scramble:doChecks: Metadata connection established.")


    ###############################################################################

    def preReqs(self):
        # what do we need?
        if self.fops.scrambleFolder():
            # Have a CSV file to work on
            self.fops.read_dataset(get_raw_file()) # store as DF
            if self.dbob.userCheck():
                self.dbob.read_seq() # store pseudo ids sequence + NTAGS
                return True
            else:
                return False
        else:
            return False
        
    ########################################################################################################################

    def thescrambler(self):
        """! @brief Scrambles dataframe and writes output to CSV file in output folder
        """
        # TODO
        ## read data file into DF
        
        ## read ntags into DF
        ## read sequence into DF
        src_file = self.get_raw_file()
        self.debug_log(f"{self.ThisVersion()} Scramble:thescrambler Source file is {src_file}")
        data_df = self.get_data()
        self.debug_log(f"{self.ThisVersion()} Scramble:thescrambler Data DF is {data_df}")
        ntags_df = self.get_ntags()
        self.debug_log(f"{self.ThisVersion()} Scramble:thescrambler NTAGs DF is {ntags_df}")
        seq_df = self.get_seq()
        self.debug_log(f"{self.ThisVersion()} Scramble:thescrambler Sequence DF is {seq_df}")
        identifier = self.get_identifier()
        self.debug_log(f"{self.ThisVersion()} Scramble:thescrambler Identifier is {identifier}")
        # Check correct identifier is in the file
        if identifier not in data_df.columns:
            self.error_log(f"{self.ThisVersion()} Scramble:thescrambler: ERROR The source, {src_file}, does not contain the identifier variable {identifier}")
            quit()

        #  Check rogue ID values.
        data_df[identifier] = data_df[identifier].astype(float)
        ntags_df[identifier] = ntags_df[identifier].astype(float)
        data_df['exists'] = data_df[identifier].isin(ntags_df[identifier]) # Produces boolean identity map (i.e. x.data -> x.ntags) 
        if data_df.exists.all() == 0: # the identity map must be complete (forall x in data_df iff x in ntags)
            self.error_log(f'{self.ThisVersion()} Scramble:thescrambler: ERROR **** Incorrect ID values in the source dataset ****')
            self.error_log(f"{self.ThisVersion()} Scramble:thescrambler: ERROR **** Bad IDs {data_df.loc[data_df.exists == 0, identifier]}")
            quit()
        # ASSUMPTION: the data_df will always have serno, but this may not be the identifier to use, a NTAG column may be needed 
        # This next bit creates a new dataframe containing the identifier and drops the boolean map column
        if identifier.casefold() != 'serno':
            # have to add the identifier from ntags_df to temp data_df
            ntags_df = ntags_df[[identifier]] # keep only the identifier column in ntags_df
            # Using NTAGn as identifier to match so merge into temp data_df
            df_temp = pd.merge(pd.DataFrame(ntags_df), pd.DataFrame(data_df), left_on=identifier, right_on=identifier, how='inner')
            df_temp = df_temp.drop(identifier, axis=1)
            # Have joined NTAG and Data dataframes on common identifier (NTAGn) 
            df_temp.sort_values(['serno'], inplace=True, ascending=True)
            df_temp['serno'] = df_temp['serno'].astype(float)
            df_temp = df_temp.drop('exists', axis=1)
        else:
            # the identifier 'serno' is already in the temp data_df and doesn't need to be added 
            df_temp = data_df
            df_temp = df_temp.drop('exists', axis=1)
        # Join dataframes temp data_df and the sequence seq_df on serno 
        self.info_log(f"{self.ThisVersion()} Scramble:thescrambler  ****** Scrambling file {src_file} ******")
        # Merge NSHD_ID with sernoed dataset and scramble the file
        df_final = pd.merge(pd.DataFrame(seq_df), pd.DataFrame(df_temp), left_on='serno', right_on='serno', how='inner')
        df_final = df_final.drop('serno', axis=1)
        df_final.sort_values(['nshdid_' + self.get_usrname()], inplace=True, ascending=True)

        # Save the file
        output = self.get_scram_file()
        try:
            df_final.to_csv(output, index=False, header=True, encoding='utf-8')  # create the csv file from the above dataframe
        except (IOError, OSError) as err:
            self.error_log(f"{self.ThisVersion()} Scramble:thescrambler: ERROR **** Cannot write to CSV file {output}")
            quit()
        self.info_log(f"{self.ThisVersion()} Scramble:thescrambler: Successfully created scrambled file {output}")
        self.dbob.record_scramble() # Record it in database

    ########################################################################################################################

