"""
Program: SDMS
   Python program to scramble a basket from an existing set of CSV files stored on Swan  
   Imran Shah LHA SST March 2019
   This version hacked by Phil Curran to work with Jay and modified Dec 2022
   This script takes five arguments: see below
   It also relies on a YAML configuration file which is set in the class constructor method

Returns:
   A zipped archive containing the scrambled dataset specified by the basket name.

Parameters:

  String command  -  scramble/unscramble
  String source   -  The file type for the input file. spss, csv or mysql
  String input    -  Filename and location or MySQL database and table name
  String id       -  Identifier used in the source dataset
  String username -  The scramble db username to which the data should be scrambled to

See Also:
    <Boomerang>, <ZIPService>

"""

from os import getenv
import csv
import time
import savReaderWriter as spss
import pandas as pd
import locale
import sys
import os
import re
import fnmatch as fn
import shutil as sh
import mysql.connector
from mysql.connector import errorcode
import psycopg2
import yaml
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.sql import select,text
import logging
from _stat import filemode
from pathlib import Path

"""
Method: Class level method to return the version string of this program.


"""
def SwiftVersion():
    return "SDUM-2.3"

"""
 Class: SooDonUhMuhs
   Does everything necessary to scramble/unscramble datasets and deliver zipped
   archives of CSV files

"""


class SooDonUhMuhs:

##############################################################################################################################

    """
    Constructor: SooDonUhMuhs
        Initialises the object with the location of the properties file and the current time which
        is used as a suffix for the CSV data file name

        See Also:
            <doChecks>
    """

    def __init__(self,method="scramble"):
        """
        File: 
            "/opt/swift/bobthebuilder.yaml"
            YAML properties file (can be shared with Swift Java sources) containing
            database configuration parameters and other settings.
        
        Property: properties
            Instance property that stores file name of Java properties file

        """
        self.properties = "/opt/swift/bobthebuilder.yaml"
        # Get properties from yaml file
        r = self.getProps()
        if r == False:
            print("Cannot read properties file",self.properties)
            quit("Cannot read properties file; stopping.")
        else:
            print("Read properties file",self.properties)
            self.props = r
        # Property: thetime
        #    Instance property that stores the current date/time to be used as a file suffix
        self.thetime = time.strftime("%Y%m%d-%H%M%S")#create a time variable which will be used in all the filenames.
        # Set up the logging file
        logging.basicConfig(filename=self.props["pyLogPath"],level=logging.DEBUG,format='%(asctime)s %(message)s')
        print("Setting up logging at",self.props["pyLogPath"])
        self.method = None     # <scramble> | <unscramble>
        self.source = None     # <SPSS> | <MySQL> | <XNAT> | <CSV> | <Stata>
        self.basketID = None   # From Swift/Skylark
        self.identifier = None # The identifier to use for matching: <SERNO> or <NTAG>
        self.user_name = None  # The scrambling database project/username
        self.filename = None   # The input file name
        self.outdir = None     # The folder used to store the scrambled output dataset
        self.fqoutfile = None  # Fully qualified path to input CSV file
        self.outfile = None    # The output file name
        # Property: path
        #    Instance property that holds the path to the folder where files will be created
        self.path = self.props["pyLinPath"]
        # Properties: 
        #   metaDB - instance property; connection object for metadata
        #   dataDB - instance property; connection object for data
        self.metaDB = None
        self.dataDB = None
        # Properties: 
        # dfData        - instance property; pandas dataframe holding dataset, 
        # df_dfVarNames - instance property; pandas dataframe holding variable names from dataset, 
        # dfVarLabels   - instance property; pandas dataframe holding value labels from dataset
        #    These properties are used to hold input data to be scrambled
        self.dfData = None
        self.dfVarNames = None
        self.dfVarLabels = None
        self.dfValueLabels = None
        # Properties: 
        # dfNtag - instance property; pandas dataframe of NTAG identifiers 
        # dfSeq  - instance property; pandas dataframe of scrambling sequence ids
        #    These properties are used to hold scrambling sequence and identifier
        #    information to be used in modifying dfData above
        self.dfNtag = None
        self.dfSeq = None
        self.doChecks() # doChecks sets a number of properties
        # Other properties used in SDMS:



######################################################################################################################
    """ 
    Method: getProps
        Read system properties from YAML properties file
        selects the 'bobthebuilder' section and returns
        this.  Returns None on failure
    """
    def getProps(self):
        # Read system properties from YAML properties file
        try:
            with open(self.properties, "r",encoding="utf-8") as f:
                p = yaml.safe_load(f)
        except:
            logging.error(SwiftVersion()+" Error reading properties file: %s",self.properties)
            return False
        # I'm just interested in the sohdonuhmuhs section of p
        sec = p['sohdonuhmuhs']
        return sec

####################################################################################
    """ 
    Method: setupAlchemy
        Makes use of the sqlalchemy package to create db connections
        to either PostgreSQL data or MySQL metadata databases.
        As as side effect assigns connections to instance properties metaDB or dataDB
        Returns True on success or False on failure
    """
    def setupAlchemy(self,dbtype,parms):
        logging.info(SwiftVersion()+" setupAlchemy called with dbtype: %s and parms: %s",dbtype,parms)
        if dbtype == "data":
            # create PostgreSQL connection with parameters
            try:
                engine = create_engine('postgresql+psycopg2://'+parms["pyPgUID"]+':'+
                                       parms["pyPgPwd"]+'@'+parms["pyPgServer"]+'/'+
                                       parms["pyPgDBase"])
            except:
                logging.error(SwiftVersion()+" Error with create engine of type: %s",dbtype)
                return False
            try:
                alccxn = engine.connect()
            except psycopg2.Error as err:
                logging.error(SwiftVersion()+" Error attempting to create cursor")
                logging.error(SwiftVersion()+' Main error: %s on cursor %s gives diagnostice message %s',err.pgerror, err.diag.message_primary, err.cursor)
                return False
            self.dataDB = alccxn
            return True
        elif dbtype == "metadata" or dbtype == "scramble":
            # create MySQL connection with parameters
            if dbtype == "metadata":
                dbname = parms["pyMyDBase"]
            else:
                dbname = parms["pyMyDBaseScram"]
            try:
                logging.info(SwiftVersion()+" Metadata DB: UID=%s Server=%s DB=%s",parms["pyMyUID"],parms["pyMyServer"],dbname)
                engine = create_engine('mysql+mysqlconnector://'+parms["pyMyUID"]+':'+
                                       parms["pyMyPwd"]+'@'+parms["pyMyServer"]+'/'+
                                       dbname)
            except:
                logging.error(SwiftVersion()+" Error with create engine of type: %s on database %s",dbtype,dbname)
                return False
            try:
                alccxn = engine.connect()
            except mysql.connector.Error as err:
                logging.error(SwiftVersion()+" Error attempting to create cursor")
                logging.error(SwiftVersion()+' Main error: %s SQLSTATE %s Diagnostice message %s',err.errno, err.sqlstate, err.msg)
                return False
            self.metaDB = alccxn
            return True
        else:
            logging.error(SwiftVersion()+" Error unspecified DB type in call to setupAlchemy")
            return False
########################################################################################################################

    """ 
    Method: setupFolder
        Makes sure the folder exists where CSV or other files will be located
        as a result of running the scramble process
        
        Now respects the suffix 'raw' or 'scrambled' to work with Swift and Jay.
        The file path is now constructed as:
            pyLinPath+os.sep+basketID+os.sep+'-SCRAMBLED'+os.sep+
        Deletes any CSV files in folder to prevent zipping previous scrambled datasets 
        being included in zip file being sent back

    Returns: True on success and False on failure
    """
    def setupFolder(self,parms):
        buildfolder = self.path+os.sep+self.basketID+os.sep+parms["pyBuildSuf"]
        logging.info(SwiftVersion()+" Raw data folder is %s", buildfolder)
        if not os.path.exists(buildfolder):
            # if there's no data to scramble stop here
            logging.error(SwiftVersion()+" Can't find input folder: %s",buildfolder)
            return False
        self.outdir = self.path+os.sep+self.basketID+os.sep+parms["pyScramSuf"]
        logging.info(SwiftVersion()+" Output folder path is %s", self.outdir)
        if not os.path.exists(self.outdir):
            # doesn't exist so have to create it but must be empty
            try:
                logging.info(SwiftVersion()+" Creating scrambling folder: %s",self.outdir)
                os.makedirs(self.outdir)
            except:
                logging.error(SwiftVersion()+" Error trying to create folder: %s",self.outdir)
                return False
        else:
            # There might be previously scrambled stuff here so get rid of it
            here = Path(self.outdir) # get a handle on the existing folder
            for child in here.iterdir():
                logging.info(SwiftVersion()+" Removing existing scrabled file: %s", child)
                child.unlink(missing_ok=True) # remove file or symbolic link
                
        # Look for evidence of existing built dataset        
        logging.info(SwiftVersion()+" Looking for files to scramble in %s",buildfolder)
        # Test if there are CSV files and copy the value_labels*.csv and variable_labels*.csv to self.outdir
        filelist = [f for f in os.listdir(buildfolder) if fn.fnmatch(f, self.basketID+'*.csv')]
        csvflist = [f for f in os.listdir(buildfolder) if fn.fnmatch(f, '*.csv') or  fn.fnmatch(f,'*Script*')]
        # Filelist includes only data files, whereas csvflist include variable/value labels/syntax files too
        if len(filelist) > 0:
            logging.info(SwiftVersion()+" Looks like there is/are %s input CSV file(s) in %s",len(filelist),buildfolder)
            # remember file names need to be turned into full path names to do the copying
            reobj = re.compile(r'^variable_.+\.csv$|^value_.+\.csv$|^STATA.+\.do$|^SPSS.+\.sps$',re.IGNORECASE)
            for f in csvflist:
                if reobj.match(f):
                    logging.info(SwiftVersion()+" Copying %s to %s",f,self.outdir+os.sep)
                    sh.copyfile(buildfolder+os.sep+f,self.outdir+os.sep+f)
        else:
            logging.info(SwiftVersion()+" Help!! Cannot find raw files to copy in %s", buildfolder)
        try:
            for f in os.listdir(buildfolder):
                # return a list containing the names of the entries in the directory given by arg
                logging.info(SwiftVersion()+" Looking at %s in directory: %s",f,buildfolder)
                if fn.fnmatch(f,self.basketID+'*.csv'):
                    # This CSV file matches the pattern and therefore is a potential file to be scrambled
                    self.filename = f
                    self.fqfilename = buildfolder+os.sep+self.filename
                    self.outfile    = self.basketID+'-SCRAMBLED.csv'
                    self.fqoutfile  = self.outdir+os.sep+self.outfile
                    logging.info(SwiftVersion()+" Found csv file: %s",self.fqoutfile)
                    return True
            return False
        except:
            logging.error(SwiftVersion()+" Error looking for files/folders in path: %s",buildfolder)
            return False


########################################################################################################################

    """  
    Method: argChecks
         Make sure arguments are valid and assign to object properties if so.
         Assigns values to self.method,self.source,self.basketID,self.identifier,self.user_name
         Return True if success, False if there is a problem

         See Also:

    """

    def argChecks(self):
        # Check correct number of arguments provided when executing the script
        if not (len(sys.argv) == 6):
            logging.error(SwiftVersion() + " Incorrect number of arguments. Exactly 5 arguments should be given.")
            return False
        else:
            # Create variables for the system arguments
            self.method = sys.argv[1].lower()  # choose the scramble or unscramble method
            self.source = sys.argv[2].lower()  # source of input file i.e SPSS, MySQL, XNAT etc...
            self.basketID = sys.argv[3]        # Basket ID
            self.identifier = sys.argv[4].lower()   # Identifier used in the source dataset
            self.user_name = sys.argv[5]       # Username from the scrambling database

        # Check the method given in the arguments is either scramble or unscramble.
        if self.method != 'scramble' and self.method != 'unscramble':
            logging.error(SwiftVersion() + " The argument, %s  has been specified. Only scramble or unscramble keywords are allowed.",method)
            return False

        # Check that only allowable ID is specified
        if  self.identifier.lower() != 'serno' and self.identifier.lower() != 'ntag1' and self.identifier.lower() != 'ntag2' and \
            self.identifier.lower() != 'ntag3' and self.identifier.lower() != 'ntag4' and self.identifier.lower() != 'ntag5' and \
            self.identifier.lower() != 'ntag6' and self.identifier.lower() != 'ntag7' and self.identifier.lower() != 'ntag8' and \
            self.identifier.lower() != 'ntag9':
            logging.error(SwiftVersion() + "The argument, %s has been wrongly specified. Only serno or ntag1 through to ntag9 are allowed.",self.identifier)
            return False
        return True

########################################################################################################################
    """ 
    Function userCheck
        Check that the username exists in the scrambling database.
        Returns True if found in scrambling database, False otherwise

        See Also:
    """
    def userCheck(self):
        # Check user exists in the users table in the scrambling database
        sqlq = ("select * from users where user  = '{name}'".format(name=self.user_name))
        results = self.metaDB.execute(sqlq).fetchall()  # Fetches basketID,Description,username,createDate
        if not results:
            logging.error(SwiftVersion()+' Cannot find user: %s  in scrambling DB: Stopping.',self.user_name)
            return False
        else:
            return True




########################################################################################################################

    """  
    Method: doChecks
         Reads database configuration parameters from a Java like properties file
         and opens connections to MySQL metadata and PostgreSQL data DBs.  Stores
         the connections as object local state variables.  In addition it checks
         that an appropriate folder exists in the local server file space and
         creates one if not present.

         See Also:

    """

    def doChecks(self):
        if not self.argChecks():
            logging.error(SwiftVersion() + "ArgChecks failed: Stopping.")
            quit()

        # Connect to swift database
        if self.setupAlchemy("scramble",self.props):
            logging.info(SwiftVersion()+" Metadata connection established.")
        else:
            logging.error(SwiftVersion()+" Metadata connection failed.")
            quit()

        if not self.userCheck():
            logging.error(SwiftVersion()+" UserCheck failed: Stopping.")
            quit()

        # Check the file exists. If so, extract the filename without the extension.
        havedata = False
        if self.source != 'mysql':
            havedata = self.setupFolder(self.props)
        logging.info(SwiftVersion()+" Result of data search %s",havedata)
        if not havedata:
            logging.error(SwiftVersion()+" Quitting due to inability to find input data.")
            quit()

    ########################################################################################################################
    """
    Method: read_csv
            Reads data from  CSV file into a pandas dataframe
            
            Returns: nothing
    """
    def read_csv(self):
        theinput = self.fqfilename
        logging.info(SwiftVersion()+" read_csv attempting to get data from %s",theinput)
        self.dfData = pd.read_csv(theinput, sep=",", header='infer', on_bad_lines='skip', index_col=False, dtype='unicode')
        self.dfVarNames = list(self.dfData.columns.values)
        self.dfVarNames = [x.lower() for x in self.dfVarNames]  # convert all column headers to lower case
        self.dfData.columns = self.dfVarNames  # rename the dataframe to use the varnames with no prefix

    ########################################################################################################################
    """
    Method: read_spss
            Reads data and metadata from an SPSS system file into a pandas dataframe
            
            Returns: nothing
    """
    def read_spss(self):
        input = self.fqoutfile
        with spss.SavReader(input, returnHeader=True, ioUtf8=0, rawMode=False) as data:
            allData = data.all()
            variables = allData[0]
            records = allData[1:]
        with spss.SavHeaderReader(input, ioUtf8=1) as header:  # UTF8 = 1 (True)
            metadata = header.dataDictionary(asNamedtuple=True)
            self.dfVarNames    = metadata.varNames       # Pull dfVarNames as list from the dictionary
            self.dfVarLabels   = metadata.varLabels      # Create a dictionary for the variable labels {Variable : Variable label}
            self.dfVarLabels   = self.dfVarLabels.values() # Split the variable label dictionary to get a list of the variable labels
            self.dfValueLabels = metadata.valueLabels    # Nested dictionary for the value labels {variable : {value:value label}}
        self.dfData = pd.DataFrame(data=records, columns=variables)
        self.dfVarNames = [x.lower() for x in self.dfVarNames]  # convert all column headers to lower case
        self.dfData.columns = self.dfVarNames           # rename the dataframe to use the varnames with no prefix

    ########################################################################################################################
    """
    Method: read_mysql
            Reads data from the PostgreSQL database into a pandas dataframe
            
            Returns: nothing
    """
    def read_mysql(self):
        self.dfData = pd.read_sql("SELECT *  FROM `%s`.`%s` " % ('nshd', self.filename), self.dataDB)
        self.dfVarNames = list(self.dfData.columns.values)
        self.dfVarNames = [x.lower() for x in self.dfVarNames]  # convert all column headers to lower case
        self.dfData.columns = self.dfVarNames  # rename the dataframe to use the varnames with no prefix

    ########################################################################################################################
    """
    Method: createCSV
            Writes the dataset to a CSV file in the correct folder
            
            Retuns: nothing
    """
    def createCSV(self):
        var_desc = list(zip(self.dfVarNames, self.dfvarLabels))
        df_temp1 = pd.DataFrame(var_desc, columns=['Name', 'Label'])
        df_variablelabels = pd.DataFrame(columns=['Name', 'Label'])
        df_variablelabels['Name'] = df_temp1['Name']
        df_variablelabels['Label'] = df_temp1['Label']
        df_variablelabels = df_variablelabels[df_variablelabels.Name.casefold() != 'serno']  # Remove the serno variable
        df_variablelabels.to_csv(self.outdir + '/variablelabels' + '-' + self.thetime + '.csv', index=False, header=True)

        df_temp2 = pd.DataFrame([(k, k1, v1) for k, v in self.dfValueLabels.items() for k1, v1 in v.items()],
                                columns=['Variable', 'Value', 'Label'])
        df_valuelabels = pd.DataFrame(columns=['Name', 'Value', 'Label'])
        df_valuelabels['Name']  = df_temp2['Variable']
        df_valuelabels['Value'] = df_temp2['Value']
        df_valuelabels['Label'] = df_temp2['Label']
        df_valuelabels['Value'] = df_valuelabels.Value.astype(str)
        df_valuelabels.to_csv(self.outdir + '/valuelabels' + '-' + self.thetime + '.csv', index=False, header=True)

    ######################################################################################################################

    """
    Method: writeSPSS
            Writes a SPSS syntax file to read the raw CSV files into SPSS
            
            Returns: nothing 
    """
    def writeSPSS(self):
        # Creating the SPSS Script
        f = open(self.outdir + '/SPSS_Script_' + '_' + str(self.thetime) + '.sps', 'w+')
        f.write('*************SPSS SCRIPT START************* \n \n \n \n')
        f.write('Note: Use SPSS Windows menu to open the CSV dataset. \n \n')
        f.write('*****SPSS Variable labels***** \n \n')
        for index, row in self.dfVarLabels.iterrows():
            f.write('VARIABLE LABELS' + ' ' + row[0] + ' ' + '"' + row[1] + '"' + "." + '\n')
        f.write('\n \n \n')
        f.write('*****SPSS Value labels***** \n \n')
        for index, row in self.dfValueLabels.iterrows():
            f.write('ADD VALUE LABELS' + ' ' + row[0] + ' ' + row[1] + ' ' + '"' + row[2] + '"' + "." + ' \n')
        f.write('\n \n \n')
        f.write('*************SPSS SCRIPT END************* \n')
        f.close()
        # End of SPSS Script
    ######################################################################################################################

    """
    Method: writeStata
            Writes a Stata syntax file to read the raw CSV files into Stata
            
            Returns: nothing
    """
    def writeStata(self):
        # Creating the STATA Script
        f = open(self.outdir + '/STATA_Script_' + '_' + str(self.thetime) + '.do', 'w+')
        f.write('*************STATA SCRIPT START************* \n \n \n \n')
        f.write('***Location of files*** \n')
        f.write('cd "Please specify the directory filepath for the files" \n \n')
        f.write('***Import dataset*** \n')
        f.write('import delim using \"specify file here\",  delim(",") varnames(1) encoding("utf-8") clear \n \n')
        f.write('***STATA Variable labels***** \n \n')
        for index, row in self.dfVarLabels.iterrows():
            f.write('label variable' + ' ' + str.lower(row[0]) + ' ' + '"' + row[1] + '"''\n')
        f.write('\n \n \n')
        f.write('***STATA value labels***** \n \n')
        f.write('**Defining value labels** \n \n')
        for index, row in self.dfValueLabels.iterrows():
            f.write('label define' + ' ' + str.lower(row[0]) + ' ' + row[1] + ' ' + '"' + row[2] + '"' + ', modify''\n')
        f.write('\n \n \n')
        f.write('**Applying defined labels** \n \n')
        valist = self.dfValueLabels['Name'].drop_duplicates().values.tolist()
        for item in valist:
            f.write('label values' + ' ' + str.lower(item) + ' ' + str.lower(item) + '\n')
        f.write('\n \n \n')
        f.write('*************STATA SCRIPT END************* \n')
        f.close()
        # End of STATA Script
    ######################################################################################################################

    """
    Method: read_seq
            Reads scramble information from the 'scramble' database 
            and sets instance properties accordingly
    """
    def read_seq(self):
        myquery = ("SELECT * FROM users WHERE user = '%s'" % (self.user_name))  # The name of our query
        Results = self.metaDB.execute(myquery).fetchall()  # Fetches all the data
        for Result in Results:
            User = Result[0]
            ProjectID = Result[1]
            Seqtable = Result[2]
            Colname = Result[3]

        self.dfSeq = pd.read_sql("SELECT serno, %s FROM %s ORDER BY serno asc" % (Colname, Seqtable), self.metaDB)  #  SQL query to pandas dataframe
        self.dfSeq = self.dfSeq.rename(columns={Colname: 'nshdid_' + self.user_name})
        self.dfSeq.serno = self.dfSeq.serno.astype(float)

        # Read the ntags file only if file not already sernoed
        if self.identifier != 'serno':
            self.dfNtag = pd.read_sql("SELECT serno, %s FROM %s ORDER BY %s asc" % (self.identifier, 'newnshdid', self.identifier), self.metaDB)
        elif self.identifier == 'serno':
            self.dfNtag = pd.read_sql("SELECT %s FROM %s ORDER BY %s asc" % (self.identifier, 'newnshdid', self.identifier), self.metaDB)

    ########################################################################################################################
    """
    Method: scramble
            Scrambles dataframe and writes output to CSV file in basket folder
    """
    def scramble(self):
        input = self.fqoutfile
        # Check correct identifier is in the file
        if self.identifier not in self.dfData.columns:
            logging.error("The source, %s, does not contain the identifier variable %s",input,self.identifier)
            quit()

        #  Check rogue ID values.
        self.dfData[self.identifier] = self.dfData[self.identifier].astype(float)
        self.dfNtag[self.identifier] = self.dfNtag[self.identifier].astype(float)
        self.dfData['exists'] = self.dfData[self.identifier].isin(self.dfNtag[self.identifier])
        if self.dfData.exists.all() == 0:
            logging.error('**** Incorrect ID values in the source dataset ****')
            logging.error("Bad IDs %s",self.dfData.loc[self.dfData.exists == 0, self.identifier])
            quit()

        if self.identifier.casefold() != 'serno':
            df_temp = pd.merge(pd.DataFrame(self.dfNtag), pd.DataFrame(self.dfData), left_on=self.identifier, right_on=self.identifier, how='inner')
            df_temp = df_temp.drop(self.identifier, axis=1)
            df_temp.sort_values(['serno'], inplace=True, ascending=True)
            df_temp['serno'] = df_temp['serno'].astype(float)
            df_temp = df_temp.drop('exists', axis=1)
        elif self.identifier.casefold() == 'serno':
            df_temp = self.dfData
            df_temp = df_temp.drop('exists', axis=1)

        logging.info(SwiftVersion()+' ****** Scrambling file ' + input + " ******")
        # Merge NSHD_ID with sernoed dataset and scramble the file
        df_final = pd.merge(pd.DataFrame(self.dfSeq), pd.DataFrame(df_temp), left_on='serno', right_on='serno', how='inner')
        df_final = df_final.drop('serno', axis=1)
        df_final.sort_values(['nshdid_' + self.user_name], inplace=True, ascending=True)

        # Save the file
        output = self.fqoutfile
        df_final.to_csv(output, index=False, header=True, encoding='utf-8')  # create the csv file from the above dataframe

    ########################################################################################################################
    
########################################################################################################################

########################################################################################################################
    """
    Method: log
            Updates the history_copy table in the scrambling database with details
            of the scrambling operation
            
            Returns: nothing
    """
    def log(self):
        input = self.fqoutfile
        Session = sessionmaker(bind=self.metaDB)
        sess = Session()
        sess.begin(subtransactions=True)
        # Logging process into the scrambling database
        sql = ("INSERT INTO history_copy (timepoint, class, input, outfile, scramuser) VALUES (NOW(),'%s','%s','%s','%s')" % (self.method, self.fqfilename, self.fqoutfile, self.user_name))
        try:
           # Execute the SQL command
           self.metaDB.execute(sql)
           # Commit your changes in the database
           sess.commit()
        except:
           # Rollback in case there is any error
           sess.rollback()
        self.metaDB.close()

########################################################################################################################
    """ 
    Method: doScramble
            Takes source as an argument and carries out the actions to scramble that kind of file
    
            Returns True on success and False on failure
    """

    def doScramble(self):
        if self.source.count('sps') > 0:
            self.read_spss()
            self.read_seq()
            self.scramble()
            self.createCSV()
            self.writeSPSS()
            self.writeStata()
            self.log()
            logging.info(SwiftVersion() + ' Scrambling process is complete.')
            return True
        elif self.source.count('csv') > 0:
            self.read_csv()
            self.read_seq()
            self.scramble()
            self.log()
            logging.info(SwiftVersion() + ' Scrambling process is complete')
            return True
        else:
            logging.error(SwiftVersion() + ' Incorrect source format. The source format specified must be \'spss\', \'csv\' or \'mysql\'.')
            return False
########################################################################################################################
    """ 
    Method: doUnScramble
            Takes source as an argument and carries out the actions to unscramble that kind of file
    
            Returns True on success and False on failure
    """
    def doUnScramble(self):
        logging.error(SwiftVersion()+' This version does not unscramble datasets.')
        return False


###End of Class Definition################################################################################################################

if __name__ == "__main__":
    sid = SooDonUhMuhs()
    logging.info(SwiftVersion()+" *********************************************")
    logging.info(SwiftVersion()+" SDMS - Scramble Dataset Program")
    logging.info(SwiftVersion()+" Method chosen = %s",sid.method)
    logging.info(SwiftVersion()+" Source dataset type = %s",sid.source)
    logging.info(SwiftVersion()+" Input identifier = %s",sid.identifier)
    logging.info(SwiftVersion()+" User ID = %s",sid.user_name)
    logging.info(SwiftVersion()+" BasketID = %s",sid.basketID)
    logging.info(SwiftVersion()+" *********************************************")
    if sid.method == 'scramble':
        if sid.doScramble():
            logging.info(SwiftVersion()+" Scrambling successful.")
        else:
            logging.error(SwiftVersion()+" Error: scrambling failed.")
    elif method == 'unscramble':
        if sid.doUnScramble():
            logging.info(SwiftVersion()+" Unscrambling successful.")
        else:
            logging.error(SwiftVersion()+" Error: unscrambling failed.")

