"""
Authors:  Imran Shah and Phil Curran
This software is open source (GPL license) but developed with
support from:
2009-2022 Medical Research Council & UCL

 About: MetaExtract.py
 Metadata Extract for SPSS and STATA datasets
 Date Started: 27th June 2018
 This version hacked by Phil Curran (2020+) to add logging and YAML config reading
 11/11/2022 - Modified to clean up CardNumber names as these become table names
 25/11/2022 - Added Method comments for Natual documentation system

Arguments required to execute job.

 1. Card number for the new dataset.
 2. SPSS/STATA filename

"""

import time
import mysql
import mysql.connector
import psycopg2
import savReaderWriter as spss
import pandas as pd
import locale
import sys
import numpy as np
import os
import re
import yaml
import logging
from psycopg2._psycopg import Error
from sqlalchemy import create_engine
from string import ascii_letters, digits


###############################################################################
def SwiftVersion():
    return "MetaExtract-2.0 "


###############################################################################

"""
Class: MetaExtract
    Reads SPSS/Stata files and extracts variable names, labels, and value labels
    in order to write them to CSV files and upload them to both MySQL metadata DB
    and PostgreSQL data DB.
    
See Also:
    <MetaUpload>
"""


class MetaExtract:
    # Property: vpat
    #   Class property RE matching SERNO or NTAGx
    vpat = re.compile(r"^([Ss][Ee][Rr][Nn][Oo]|[Nn][Tt][Aa][Gg]\d|[Ss][Ee][Xx]|[Ii][Nn][Ff])$")
    # Property: VALIDCHARS
    #   Class property enumerating the valid characters for field/variable names
    VALIDCHARS = ascii_letters + digits + '_'
    # Property: NARGS
    #   Class property stating the number of required command line arguments to this program
    NARGS = 2

    """
    Method: ExtAlphanum
        Returns a string with invalid characters removed
    """
    def ExtAlphanum(InputString):
        return "".join([ch for ch in InputString if ch in MetaExtract.VALIDCHARS])

    """
    Method: CleanUp
        Apply ExtAlphanum to the keys of a ditionary
        Returns cleaned up dictionary or original value if not dict

    See Also:
        <ExtAlphanum>
    """
    def CleanUp(adict):
        newDict = dict({})
        if isinstance(adict,dict):
            # do something if a dictionary
            for key, value in adict.items():
                newDict.update({MetaExtract.ExtAlphanum(key): value})
            return newDict
        else:
            # otherwise give back what you're given
            return adict

    """
    Method: getProps
        Read system properties from YAML properties file
        selects the 'metadata' section and returns
        this.  Returns None on failure
    """

    def getProps(self, section):
        # Read system properties from YAML properties file
        try:
            with open(self.properties, "r", encoding="utf-8") as f:
                p = yaml.load(f, yaml.SafeLoader)
        except:
            logging.error(SwiftVersion() + " Error reading properties file: %s", self.properties)
            return None
        # I'm just interested in the relevant section of p
        sec = p[section]
        return sec
    """
    Constructor: MetaExtract.__init__
                 Initialises instance varibles, starts logging to a file, 
                 calls getProps to read YAML config file,
                 sets path for CSV file creation, sets up empty Numpy DataFrames
                 for variable labels and value labesl, and calls doChecks

    See Also:
        <doChecks>, <getProps>
    """
    def __init__(self, propfile):
        # Property: inputfile
        #   Instance property that holds path to SPSS/Stata file
        self.inputfile = None
        # Property: CardNumber
        #   Instance property that names the Library File metadata will be stored under
        self.CardNumber = None
        # Property: metaDB, dataDB
        #    Instance property that holds a valid SQLAlchemy connection object
        self.metaDB = None
        self.dataDB = None
        self.varNames = None  # A list of variable names
        self.varLabels = None # A dict of {varname: label}
        self.valLabels = None # A dict of {varname: {value: value label}}
        self.dfdata = None # A Pandas DataFrame
        # Property: df_variablelabels
        #   Instance property holding dataframe of variable names and labels
        self.df_variablelabels = None
        # Property: df_valuelabels
        #   Instance property holding dataframe of value labels and codes
        self.df_valuelabels = None
        # Property: properties
        #   Instance property holding the file path to the YAML file
        self.properties = propfile
        # Get properties from yaml file
        r = self.getProps('metadata')
        if r is None:
            quit()
        else:
            self.props = r
        # Set up the logging file
        logging.basicConfig(filename=self.props["LogPath"], level=logging.DEBUG, format='%(asctime)s %(message)s')
        logging.info("+++++++++++ Start of Run ++++++++++++")
        # Property: metaDB, dataDB
        #    Instance property that holds a valid SQLAlchemy connection object
        self.metaDB = None
        self.dataDB = None
        self.varNames = None
        self.varLabels = None
        self.valLabels = None
        # Property: path
        #    Instance property that holds the path to the output folder where files will be created
        self.path = self.props["CSVPath"]
        logging.info(SwiftVersion() + "Folder will be create at %s if it doesn't exist.", self.path)
        # Property: thetime
        #    Instance property that stores the current date/time to be used as a file suffix
        self.thetime = time.strftime("%Y%m%d-%H%M%S")  # create a time variable which will be used in all the filenames.
        # Creating the variable labels, value labels, category membership and filepath dataframes for each of the
        # swift tables.
        """
        VARIABLELABELS TABLE STRUCTURE
        ['CardNumber', 'ColStart', 'ColEnd', 'Name', 'Label', 'Form', 'QuestionNumber',
        'YEAR', 'CodeBookNumber', 'Derived', 'Verified', 'Uncoded', 'Available',
        'Recommended', 'ReplaceWith', 'Creator', 'CreateDate', 'CreatorApp', 'Public']
        VALUELABELS TABLE STRUCTURE
        ['Name', 'Value', 'Label', 'MissingValueCode', 'Creator', 'CreateDate', 'CreatorApp']

        """
        self.df_variablelabels = pd.DataFrame(columns=['CardNumber', 'ColStart', 'ColEnd', 'Name', 'Label',
                                                       'Form', 'QuestionNumber', 'YEAR', 'CodeBookNumber', 'Derived',
                                                       'Verified', 'Uncoded', 'Available', 'Recommended', 'ReplaceWith',
                                                       'Creator', 'CreateDate', 'CreatorApp', 'Public'])
        self.df_valuelabels = pd.DataFrame(columns=['Name', 'Value', 'Label', 'MissingValueCode', 'Creator',
                                                    'CreateDate', 'CreatorApp'])
        self.doChecks()

    """ Method: setupFolder
            Makes sure the folder exists where CSV or other files will be created 
            as a result of running this process.
            The file path is now constructed as:
            pyLinPath+os.sep+basketID+os.sep+'raw'+os.sep+
            
            Returns True on success and False on failure
    """

    def setupFolder(self, parms):
        # Create a folder to save the created files
        try:
            # Check to see output folder already created
            buildfolder = self.path + os.sep + self.CardNumber
            if not os.path.exists(buildfolder):  # Create folder called CardNumber if it does not exist.
                logging.info(SwiftVersion() + " Creating folder: %s ", buildfolder)
                os.makedirs(buildfolder)
                self.path = buildfolder  # update path
                return True
            else:
                logging.info(SwiftVersion() + " Directory %s already exists.", buildfolder)
                self.path = buildfolder
                return True
        except OSError as err:
            logging.error(SwiftVersion() + "Can't create files/folders in path: %s", buildfolder)
            logging.error(SwiftVersion() + "Error: %s", err)
            return False

    """  Method: doChecks
         Reads database configuration parameters from a Java like properties file
         and opens connections to MySQL metadata and PostgreSQL data DBs.  Stores
         the connections as object local state variables.  In addition it checks 
         that an appropriate folder exists in the local server file space and 
         creates one if not present.

         See Also:
             <setupAlchemy> <setupFolder>
    """

    def doChecks(self):
        # Check correct number of arguments provided when executing the script
        # This version expects arg1 = path to SPSS/Stata file, arg2 = path to CSV output folder
        if len(sys.argv) - 1 != MetaExtract.NARGS:
            logging.error(SwiftVersion() + " Wrong number of arguments to program; need 2 got %s.",
                          len(sys.argv) - 1)
            quit()
        #    # Create variables for the arguments provided
        self.CardNumber = sys.argv[1]  # Card number -- Better check the name!!!
        self.CardNumber = MetaExtract.ExtAlphanum(self.CardNumber) # Get rid of illegal chars in table name
        self.inputfile = sys.argv[2]  # Full path for the SPSS/STATA dataset
        logging.info(SwiftVersion() + "Running MetaExtract for CardNumber: %s with input file %s",
                     self.CardNumber, self.inputfile)
        # Connect to swift/skylark database
        if self.setupAlchemy("metadata", self.props):
            logging.info(SwiftVersion() + " Metadata connection established.")
        else:
            logging.error(SwiftVersion() + " Metadata connection failed.")
            quit()
        # Connect to postgresql data
        if self.setupAlchemy("data", self.props):
            logging.info(SwiftVersion() + " Data connection established.")
        else:
            logging.error(SwiftVersion() + " Data connection failed.")
            quit()
        # Check folder can be created or exists
        if not self.setupFolder(self.props):
            logging.error(SwiftVersion() + " Error setting up folder to write files to: %s", self.path)
            quit()

    """ Method: setupAlchemy
        Makes use of the sqlalchemy package to create db connections
        to either PostgreSQL data or MySQL metadata databases.
        As as side effect assigns connections to instance properties metaDB or dataDB
        Returns True on success or False on failure
    """

    def setupAlchemy(self, dbtype, parms):
        if dbtype == "data":
            # create PostgreSQL connection with parameters
            try:
                engine = create_engine('postgresql+psycopg2://' + parms["pyPgUID"] + ':' +
                                       parms["pyPgPwd"] + '@' + parms["pyPgServer"] + '/' +
                                       parms["pyPgDBase"])
            except:
                logging.error(SwiftVersion() + " Error with create engine of type: %s", dbtype)
                return False
            try:
                alccxn = engine.connect()
            except psycopg2.Error as err:
                logging.error(SwiftVersion() + " Error attempting to create cursor")
                logging.error(SwiftVersion() + ' Main error: %s on cursor %s \nDiagnostice message %s', err.pgerror,
                    err.diag.message_primary, err.cursor)
                return False
            self.dataDB = alccxn
            return True
        elif dbtype == "metadata":
            # create MySQL connection with parameters
            try:
                logging.info(SwiftVersion() + " Metadata DB: UID=%s Server=%s DB=%s", parms["pyMyUID"], parms["pyMyServer"],
                             parms["pyMyDBase"])
                engine = create_engine('mysql+mysqlconnector://' + parms["pyMyUID"] + ':' +
                                       parms["pyMyPwd"] + '@' + parms["pyMyServer"] + '/' +
                                       parms["pyMyDBase"])
            except Exception as err:
                logging.error(SwiftVersion() + " Error with create engine of type: %s", dbtype)
                logging.error(SwiftVersion() + "Error returned: %s", err)
                return False
            try:
                alccxn = engine.connect()
            except mysql.connector.Error as err:
                logging.error(SwiftVersion() + " Error attempting to create cursor")
                logging.error(SwiftVersion() + ' Main error: %s SQLSTATE %s \nDiagnostice message %s', err.errno,
                              err.sqlstate, err.msg)
                return False
            self.metaDB = alccxn
            return True
        else:
            logging.info(SwiftVersion() + " Error unspecified DB type in call to setupAlchemy")
            return False

    """ Method: createCSV
                  Writes Metadta to CSV files
    """


    def createCSV(self):
        # Recall self.varLabels and self.valLabels are dicts and df_variablelabels is a Pandas Dataframe
        thedata = list(zip(self.varLabels.keys(),self.varLabels.values()))
        df_temp1 = pd.DataFrame(thedata, columns=['Name', 'Label'], index=self.varNames)
        self.df_variablelabels['Name'] = df_temp1['Name']
        self.df_variablelabels['Label'] = df_temp1['Label']
        self.df_variablelabels['Derived'] = 0
        self.df_variablelabels['Creator'] = 'PC/IS'
        self.df_variablelabels['CreatorApp'] = 'Python'
        self.df_variablelabels['Public'] = 1
        self.df_variablelabels['CardNumber'] = self.CardNumber
        loci = self.path
        file1 = self.CardNumber + "_variablelabels.csv"
        file2 = self.CardNumber + "_valuelabels.csv"
        logging.info(SwiftVersion() + "Creating the %s file at %s", file1, loci)
        labswithids = [x for x in self.varLabels.keys() if MetaExtract.vpat.match(x)]
        self.df_variablelabels = self.df_variablelabels.drop(labswithids, axis=0)  # Remove the serno variable
        self.df_variablelabels.to_csv(loci + os.sep + file1, index=False, header=True)
        # Recall that self.valLabels and self.varLabels are dicts
        valswithids = [x for x in self.valLabels.keys() if MetaExtract.vpat.match(x)]
        # Remove the id variables
        for k in valswithids:
            self.valLabels.pop(k)
        df_temp2 = pd.DataFrame([(k, k1, v1) for k, v in self.valLabels.items() for k1, v1 in v.items()],
                                columns=['Variable', 'Value', 'Label'])
        self.df_valuelabels['Name'] = df_temp2['Variable']
        self.df_valuelabels['Value'] = df_temp2['Value']
        self.df_valuelabels['Label'] = df_temp2['Label']
        self.df_valuelabels['MissingValueCode'] = 0
        self.df_valuelabels['Creator'] = 'PC/IS/AM'
        self.df_valuelabels['CreatorApp'] = 'Python'
        logging.info(SwiftVersion() + "Creating the %s file at %s", file2, loci)
        self.df_valuelabels.to_csv(loci + os.sep + file2, index=False, header=True)

    """
    Method: read_stata
            Reads metadata and data from Stata system files and
            assigns metadata to self.VarNames, self.VarLabels, self.ValLabels,
            and writes the data to a properly headed CSV file.
    
    """


    def read_stata(self):
        logging.info(SwiftVersion() + 'Processing Stata file %s', self.inputfile)
        # open the STATA file and save file the data into a pandas dataframe
        reader = pd.read_stata(self.inputfile, iterator=True)  # Return StataReader object
        dfdata = reader.read() # Read Stata Data into Pandas DataFrame
        # Create the data CSV file
        dfdata.to_csv(self.path + os.sep + self.CardNumber + '_data.csv', index=False, header=True)
        var_lab_dict = reader.variable_labels()  # Create a dictionary for the variable labels {Variable : Variable label}
        value_lab_dict =  reader.value_labels()
        # we need to clean these up removing bad characters.
        self.varLabels = MetaExtract.CleanUp(var_lab_dict) # varLabels is a dict
        self.valLabels = MetaExtract.CleanUp(value_lab_dict) # valLabels nested dictionary {variable : {value:value label}}
        self.varNames = var_lab_dict.keys()  # Split the variable label dictionary to get a list of the variable names
        logging.info(SwiftVersion() + 'Stata value labels: %s', self.valLabels)
        
        for vars in self.varNames:
            if dfdata[vars].dtype == np.dtype('datetime64[ns]'):
                dfdata[vars] = dfdata[vars].dt.strftime('%H:%M:%S')
        dfdata.replace({'NaT': ''}, inplace=True)
        dfdata.to_csv(self.path + os.sep + self.CardNumber + '_data.csv', index=False, header=True,
                      encoding='utf-8')
        self.dfdata = dfdata

 

    """ 
    Method: read_spss
              Reads metadata and data from SPSS system (.sav) file and
              assigns metadata to self.VarNames, self.VarLabels, self.ValLabels,
              and writes the data to a properly headed CSV file.
              
    """
    
    def read_spss(self):
        logging.info(SwiftVersion() + 'Processing SPSS file %s', self.inputfile)
        """
        About: SavReader
        First record contains variable names, subsequent records contain the data
        That's why we get the expression allData[1:] below to get just the data
        """
        with spss.SavReader(self.inputfile, returnHeader=True, ioUtf8=True) as reader:
            allData = reader.all()  # Get data records
            variables = reader.getHeader(None)  # Get variable names
            self.varNames = [MetaExtract.ExtAlphanum(x) for x in variables]  # remove crap characters
            self.dfdata = pd.DataFrame(data=allData[1:], columns=self.varNames)

        """
        About: SavHeaderReader
        Using SavHeaderReader, You can look for: dict_keys(
        ['varNames', 'varTypes', 'valueLabels', 'varLabels',
        'formats', 'missingValues', 'measureLevels', 'columnWidths',
        'alignments', 'varSets', 'varRoles', 'varAttributes', 'fileAttributes',
        'fileLabel', 'multRespDefs', 'caseWeightVar'])
        """
        with spss.SavHeaderReader(self.inputfile, ioUtf8=True) as reader:
            metadata = reader.all(asNamedtuple=True)
            self.varLabels = metadata.varLabels  # variable labels {variable: label} dict
            self.valLabels = metadata.valueLabels  # value labels {variable : {value:value label}} dict
            # we need to clean these up in the same way we did for variable names.
            self.varLabels = MetaExtract.CleanUp(self.varLabels) # varLabels is a dict
            self.valLabels = MetaExtract.CleanUp(self.valLabels) # valLabels is a dict
        # Convert SERNO or NTAG to Integer format in the Numpy Dataframe
        colsswithids = [x for x in self.varNames if MetaExtract.vpat.match(x)]
        for col in colsswithids:
            logging.info(SwiftVersion() + "Converting %s to numeric", col)
            self.dfdata[col] = pd.to_numeric(self.dfdata[col])
        # Write numeric data as Dataframe to CSV file
        self.dfdata.to_csv(self.path + os.sep + self.CardNumber + '_data.csv', index=False, header=True,
                           encoding='utf-8')



    """
    Method: uploadData
              Takes data in instance variable self.dfdata (DataFrame) and uses Pandas function to_sql
              to create a PostgreSQL table with the name given by instance variable self.CardNumber
    """

    def uploadData(self):
        tableref = str.lower(self.CardNumber)
        logging.info(SwiftVersion() + "Uploading data to PostgreSQL table: %s", tableref)
        try:
            self.dfdata.to_sql(name=tableref, con=self.dataDB, index=False, if_exists='replace')
        except Error as err:
            logging.error(SwiftVersion() + "Error writing to data DB:  %s", err)
            quit()
        logging.info(SwiftVersion() + "Table created in data DB: %s", self.CardNumber)


    """
    Method: uploadMeta
              Takes metadata from instance variables self.varLabels (dict) and
              self.valLabels (dict) and from these APPENDS to the MySQL tables
              variablelabels and valuelabels
    """

    def uploadMeta(self):
        logging.info(SwiftVersion() + 'Updating the variablelabels table.')
        # Recall that self.variablelabels is an empty Pandas Dataframe
        """
        Need to populate self.variablelabels from the self.varLabels dict
        varLabels is a dict of the form { varName : varLabel, ...}
        Pandas has a useful 'append' function    
        """
        notthese = [x for x in self.varLabels.keys() if MetaExtract.vpat.match(x)]
        for k in notthese:
            self.varLabels.pop(k)
        thedata = list(zip([self.CardNumber] * len(self.varLabels.keys()),
                           self.varLabels.keys(),
                           self.varLabels.values(),
                           ['PC/IS'] * len(self.varLabels.keys()),
                           [self.thetime] * len(self.varLabels.keys()),
                           ['Python'] * len(self.varLabels.keys()),
                           [1] * len(self.varLabels.keys())))
        data = pd.DataFrame(thedata, columns=['CardNumber', 'Name', 'Label',
                                              'Creator', 'CreateDate', 'CreatorApp', 'Public'])
        self.df_variablelabels = data.copy()
        """
        VARIABLELABELS TABLE STRUCTURE
        ['CardNumber', 'ColStart', 'ColEnd', 'Name', 'Label', 'Form', 'QuestionNumber',
        'YEAR', 'CodeBookNumber', 'Derived', 'Verified', 'Uncoded', 'Available',
        'Recommended', 'ReplaceWith', 'Creator', 'CreateDate', 'CreatorApp', 'Public']
        VALUELABELS TABLE STRUCTURE
        ['Name', 'Value', 'Label', 'MissingValueCode', 'Creator', 'CreateDate', 'CreatorApp']

        """
        data['CreateDate'] = data['CreateDate'].astype('datetime64[ns]')
        data.to_sql(name='variablelabels', con=self.metaDB, if_exists='append', index=False)
        # Uploading Value labels
        logging.info(SwiftVersion() + 'Updating the valuelabels table.')
        thedata = []
        for name, vallist in self.valLabels.items():
            for label, value in vallist.items():
                thedata.append([name,label,value,'PC/IS',self.thetime,'Python'])
        data = pd.DataFrame(thedata, columns=['Name', 'Value', 'Label', 'Creator',
                                                    'CreateDate', 'CreatorApp'])
        self.df_valuelabels = data.copy()
        data['CreateDate'] = data['CreateDate'].astype('datetime64[ns]')
        data.to_sql(name='valuelabels', con=self.metaDB, if_exists='append', index=False)
        # Uploading filepath table
        logging.info(SwiftVersion() + 'Updating the filepaths table.')
        data = pd.DataFrame(columns=['CardNumber', 'path', 'SERNO', 'Creator', 'CreateDate', 'CreatorApp'])
        data.loc[-1] = [self.CardNumber, 'Postgres table ' + self.CardNumber, '', 'PC/IS/AM', self.thetime, 'Python']
        data['CreateDate'] = data['CreateDate'].astype('datetime64[ns]')
        data.to_sql(name='filepath', con=self.metaDB, if_exists='append', index=False)


#####################################################################################################################################################
"""
    Function: main
        When called as a stand alone program this creates an instance called mex
        with the name of the YAML config file to read from

    Parameters:
      -  input file
      -  path to output folder
"""
if __name__ == "__main__":
    mex = MetaExtract('bobthebuilder.yaml')
    logging.info(SwiftVersion() + ' Extract metadata from {arg} for upload to NSHD Repository:'.format(arg=sys.argv[1]))
    logging.info(SwiftVersion() + ' Files will be created with the time stamp of: {tim}'.format(tim=mex.thetime))
    # Pick your poison
    if mex.inputfile.endswith('.sav'):
        mex.read_spss()
    elif mex.inputfile.endswith('.dta'):
        mex.read_stata()
    else:
        logging.error(SwiftVersion() + 'Incorrect file format. The file must be either a STATA or SPSS file')
        quit()
    logging.info(SwiftVersion() + "Creating Metadata CSV files.")
    mex.createCSV()
    mex.uploadMeta()
    mex.uploadData()
    logging.info(SwiftVersion() + 'Metadata extraction is complete')
