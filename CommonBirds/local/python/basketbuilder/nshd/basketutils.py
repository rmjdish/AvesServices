import csv
import time
import pandas as pd
import locale
import sys
import numpy as np
import psycopg2
import os
#import fnmatch as fn
import time
from sqlalchemy.sql import select, text
# import package components
from . commondefs import *
from . dbutils  import DBUtils
from . fileutils import FileOps



class BasketBuilder(CommonDefs):
    """! @brief  Does everything necessary to build NSHD baskets and deliver zipped 
       CSV files of datasets and metadata as the data repository end of the 
       ucl.serice.zip package
    """
    def __init__(self,isa="undefined"):
        """! @brief Constructor
            Initialises the object with the location of the properties file and the current time which
            is used as a suffix for the CSV data file name
        """
        super().__init__()  # This now initializes the shared state CommonDefs<==>BasketBuilder
        fops = FileOps()
        self.props = self.get_props()
        # Property: isa
        #    Instance property that records whether basket or trolley
        self.isa = isa
        # Property: metaDB, dataDB
        #    Instance property that holds a valid SQLAlchemy connection object
        self.metaDB = None
        self.dataDB = None
        # Property: Results
        #    Instance property that holds table of information about a basket
        self.Results = None
        # Property: done
        #    Instance property that holds a boolean value on whether the basket has been previously built
        self.done = False
        # Property: df_new
        #   Instance property holding dataframe of variable names and labels for basket variables only
        self.df_new = None
        self.doChecks() # doChecks sets property bskname
        # Property: thetime
        #    Instance property that stores the current date/time to be used as a file suffix
        self.thetime = get_thetime() #create a time variable which will be used in all the filenames.


    ###############################################################################

    def kind(self, bskname):
        if 'TT' in bskname:
            return "trolley"
        else:
            return "basket"

    ###############################################################################
    def whatkind(self):
        return self.isa
    ###############################################################################
    def setIsa(self, kindof):
        if kindof == "basket" or kindof == "trolley":
            self.isa = kindof
    ###############################################################################
    def getIsa(self):
        return self.isa
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
            mdb  = DBUtils("metadata")
            self.metaDB = self.get_metadbase()
        except (MetaOpenError, MetaAccessError) as err:
            """ The following is python 3.10+
            match err:
                case MetaOpenError():
                    error_log(f"{ThisVersion()} Metadata connection failed: {err}")
                    quit()
                case MetaAccessError():
                    error_log(f"{ThisVersion()} Metadata connection failed: {err}")
                    quit()
                case DBaseError():
                    error_log(f"{ThisVersion()} Metadata other error: {err}")
                    quit()
                case _:
                    error_log(f"{ThisVersion()} Unknown error {err}")
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
                
        self.info_log(self.ThisVersion()+" Metadata connection established.")
        # Connect to postgresql data
        try:
            dbd = DBUtils("data")
            self.dataDB = self.get_datadbase()
        except (DataOpenError, DataAccessError) as err:
            """ The following is python 3.10+
            match err:
                case DataOpenError():
                    error_log(f"{ThisVersion()} Data connection failed: {err}")
                    quit()
                case DataAccessError():
                    error_log(f"{ThisVersion()} Data connection failed: {err}")
                    quit()
                case DBaseError():
                    error_log(f"{ThisVersion()} Data other error: {err}")
                    quit()
                case _:
                    error_log(f"{ThisVersion()} Unknown error {err}")
                    quit()
            """
            # Python <= 3.9
            if isinstance(err, DataOpenError):
                self.error_log(f"{self.ThisVersion()} Data connection failed: {err}")
                quit()
            elif isinstance(err, DataAccessError):
                self.error_log(f"{self.ThisVersion()} Data connection failed: {err}")
                quit()
            elif isinstance(err, DBaseError):
                self.error_log(f"{self.ThisVersion()} Data other error: {err}")
                quit()
            else:
                self.error_log(f"{self.ThisVersion()} Unknown error {err}")
                quit()
            
        self.info_log(f"{self.ThisVersion()} Data connection established.")
        # Check basket is present in the swift database
        self.setIsa(self.kind(self.get_bskname()))
        self.info_log(f"{self.ThisVersion()} Type: {self.getIsa()}; Name: {self.get_bskname()}")
        # Check folder can be created or already  exists
        fops = FileOps() # Must be after call to set_bskname
        if not fops.setupFolder():
            self.error_log(f"{self.ThisVersion()} Error setting up folder to write files to: {self.get_raw_folder()}")
            quit()
        # pull out selected basket
        # Need to define the table that will be "operated" on
        """
        basketdetails = Table('basketdetails', metadata,
                              Column('basketID',String, primary_key=True),
                              Column('Description', String),
                              Column('username', String),
                              Column('createDate', DateTime)
                              )
        """
        sqlq = text(f"select * from basketdetails where basketID = {self.get_bskname()} ")
        self.debug_log(f"{ThisVersion()} doChecks about to execute {sqlq}")
        self.debug_log(f"{ThisVersion()} doChecks excecution of {str(type(self.metaDB))}")
        self.Results = self.metaDB.execute(sqlq).fetchall()  # Fetches basketID,Description,username,createDate
        self.info_log(f"{ThisVersion()} Search results for basket: {self.Results}")
        if not self.Results and self.getIsa() == "basket":
            self.error_log(f"{self.ThisVersion()} This basket does not exist in the database: {self.get_bskname()}")
            quit()


    ###############################################################################
    def thebuilder(self):
        """! @brief Creates a temporary PostgreSQL table with one field corresponding to the variable name, 
               then creates the metadata files and calls the PostgreSQL function *datawrite* to create
               the data CSV file.  
        """

        def ExtractAlphanumeric(InputString):
            from string import ascii_letters, digits
            return "".join([ch for ch in InputString if ch in (ascii_letters + digits)])

        fops = FileOps() # get access to file utils
        db = DBUtils("metadata") # get access to DB utils
        db.getMetaData() # Causes basket metatdata to be pulled from DB
        # The basic metadata is now available so pull a copy into this object
        self.df_new = self.get_data_dict() # returns a Pandas DataFrame
        self.debug_log(f"{self.ThisVersion()} thebuilder: Data Dictionary DataFrame: {self.df_new.head()}")
        self.debug_log(f"{self.ThisVersion()} thebuilder: Data dictionary DataFrame dims: {self.df_new.shape}")
        if self.df_new is None:
            self.error_log(f"{self.ThisVersion()} thebuilder: Empty Data Dictionary. Can't proceed.")
            quit()
        else:
            self.debug_log(f"{self.ThisVersion()} thebuilder: Data Dictionary dims are {self.df_new.shape}")
        fops.writeSPSS() # File operation to create SPSS syntax file
        fops.writeStata() # File operation to create Stata syntax file
        del self.df_new['label'] #deleting the variable label column as not needed
        self.df_new.columns=['varname'] #renaming the variable name column from name to varname
        #transforming all the variable names to a lower case to match the data database
        self.df_new['varname']=self.df_new['varname'].str.lower() 
        """
        About: Create a PostgreSQL table with just the variable names to use
            as a parameter to the SQL procedure datawrite
        """
        try:
            # saving the varname into the postgres table named after the basket ID
            # make sure the name is acceptable for a PG table
            tabname = str.lower(ExtractAlphanumeric(get_bskname()))
            self.df_new.to_sql(name=tabname,
                               con=self.dataDB,
                               index=False,
                               if_exists='replace')
        except Exception as err:
            self.error_log(f"{self.ThisVersion()} Error pushing panda data frame to PG table: {tabname}")
            quit()
        self.info_log(f"{self.ThisVersion()} Created table of variable names [dims]: {self.df_new.shape}")
        # note table name is turned to lower case as the cur.execute command is looking for a lower case table name.
        # changing column type for varname to varchar from text. Is required for the postgres function extractdata and writedata
        # Notice the wierd requirement to embed the sql in 3 consequtive double quote marks.
        try:
            self.dataDB.execute(text(f"ALTER TABLE {tabname} ALTER COLUMN varname TYPE varchar"))
        except psycopg2.Error as e:
            self.error_log(f"{self.ThisVersion()} Error attempting to ALTER TABLE: {tabname}\n")
            self.error_log(f"{self.ThisVersion()} Main error: {e.pgerror} on cursor {e.cursor} \nDiagnostic message {e.diag.message.primary}")
            quit()

        #note table name is turned to lower case as the cur.execute command is looking for a lower case table name.

        file = self.get_raw_folder()+os.sep+self.get_bskname()+'_'+str(self.thetime)+'.csv'
        #run the extractdata function in postgres see http://initd.org/psycopg/docs/usage.html#query-parameters
        try:
            # run the writedata function in postgres
            self.info_log(f"{self.ThisVersion()} Calling psql datawrite with table: {tabname} and file: '{file}'")
            self.dataDB.execute(text(f"select datawrite('{tabname}', '{file}')"))
        except psycopg2.Error as e:
            self.error_log(f"{self.ThisVersion()} Error calling datawrite: {e.pgerror}")
            quit()
        # clean up the table created via to_sql above
        self.debug_log(f"{self.ThisVersion()} cleaning up by executing DROP TABLE '{tabname}'")
        try:
             self.dataDB.execute(text(f"DROP TABLE {tabname}"))
        except psycopg2.Error as e:
            self.error_log(f"{self.ThisVersion()} Error {e.pgerror} cleaning up table '{tabname}': {e.diag.message.primary} ")
            

        ##################### End #######################################
