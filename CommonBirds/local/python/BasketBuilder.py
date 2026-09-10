#!/usr/bin/python
"""
// Author:  Imran Shah
// Copyright (C) 2009-2019 Medical Research Council

 About: BobTheBuilder
 Python script to build a basket from an existing PostgreSQL table with format 
 Imran Shah LHA SST August 2017
 This version hacked by Phil Curran to modify parameters for use by Swift 2.0
 
 column 1 -- varname character varying
 
 It creates this table from the supplied basket provided it can find it in the basketdetails
 table.

 This script takes two arguments: the name of the basket and the Swift username
 It will create a folder with this name if one does not already exist
 All built CSV and syntax files will be stored here with a time stamp as part of their name
 This version expects arg1 = basketID, arg2 = Swift username
 It also relies on a YAML configuration file which is set in the class constructor method
"""
import mysql.connector
from mysql.connector import errorcode
import csv
import time
import pandas as pd
import locale
import sys
import numpy as np
import psycopg2
import os
import fnmatch as fn
import time
import yaml
from sqlalchemy import create_engine
from sqlalchemy.sql import select,text
import logging


###############################################################################
def SwiftVersion():
    return "BasketBuilder-2.2"

###############################################################################
"""
 Class: BasketBuilder
   Does everything necessary to build Swift20 baskets and deliver zipped 
   CSV files of datasets and metadata as the data repository end of tyhe 
   ucl.serice.zip package

 See Also:
    <Boomerang>, <ZipService>
"""
class BasketBuilder:
#################################################################################################################################################################################

    """
    Constructor: BasketBuilder
        Initialises the object with the location of the properties file and the current time which
        is used as a suffix for the CSV data file name

    See Also:
        <doChecks>
    """
    def __init__(self,isa="undefined"):
        # File: "/nfs/home1/swift/swift_download.properties"
        #    Java like properties file (can be shared with Swift Java sources) containing
        #    database configuration parameters and other settings.
        # Property: properties
        #    Instance property that stores file name of properties file
        self.properties = "/opt/swift/bobthebuilder.yaml"
        # Get properties from yaml file
        r = self.getProps()
        if r is None:
            quit()
        else:
            self.props = r
        # Set up the logging file
        logging.basicConfig(filename=self.props["pyLogPath"],level=logging.DEBUG,format='%(asctime)s %(message)s')
        # Property: isa
        #    Instance property that records whether basket or trolley
        self.isa = isa
        # Property: metaDB, dataDB
        #    Instance property that holds a valid SQLAlchemy connection object
        self.metaDB = None
        self.dataDB = None
        # Property: path
        #    Instance property that holds the path to the folder where files will be created
        self.path = self.props["pyLinPath"]
        # Property: done
        #    Instance property that holds a boolean value on whether the basket has been previously built
        self.done = False
        # Property: df_new
        #   Instance property holding dataframe of variable names
        self.df_new = None
        # Property: bskname
        #    Instance property that holds the name of the basket being built
        self.doChecks() # doChecks sets property bskname
        # Property: thetime
        #    Instance property that stores the current date/time to be used as a file suffix
        self.thetime = time.strftime("%Y%m%d-%H%M%S")#create a time variable which will be used in all the filenames.


###############################################################################

    def kind(self,bskname):
        if 'TT' in bskname:
            return "trolley"
        else:
            return "basket"
###############################################################################
    def whatkind(self):
        return self.isa
###############################################################################
    def setIsa(self,kindof):
        if kindof == "basket" or kindof == "trolley":
            self.isa = kindof
###############################################################################
    def getIsa(self):
        return self.isa
###############################################################################
    """ 
    Function: getProps
        Read system properties from YAML properties file 
        selects the 'bobthebuilder' section and returns
        this.  Returns None on failure
    """
    def getProps(self):
        # Read system properties from YAML properties file
        try:
            with open(self.properties, "r",encoding="utf-8") as f:
                p = yaml.load(f,yaml.SafeLoader)
        except:
            logging.error(SwiftVersion()+" Error reading properties file: %s",self.properties)
            return None
        # I'm just interested in the bobthebuilder section of p
        sec = p['bobthebuilder']
        return sec
###############################################################################

    """ 
    Function: dataExists
        Takes self.path and tests if a built CSV file exist at the end
        of the path with the basket name as the prefix.
        Returns true if such a file exists, false otherwise
    """
    def dataExists(self):
        candidates = [f for f in os.listdir(self.path) if fn.fnmatch(f,self.bskname+'*.csv')]
        if (len(candidates) < 1):
            logging.error(SwiftVersion()+" Basket file does not exist yet.")
            return False
        else:
            logging.info(SwiftVersion()+" One or more basket files exist : %s",candidates)
            return True

###############################################################################

    """ 
    Function: setupAlchemy
        Makes use of the sqlalchemy package to create db connections
        to either PostgreSQL data or MySQL metadata databases.
        As as side effect assigns connections to instance properties metaDB or dataDB
        Returns True on success or False on failure
    """
    def setupAlchemy(self,dbtype,parms):
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
                logging.error(SwiftVersion()+' Main error: %s on cursor %s \nDiagnostice message %s',err.pgerror, err.diag.message_primary, err.cursor)
                return False
            self.dataDB = alccxn
            return True
        elif dbtype == "metadata":
            # create MySQL connection with parameters
            try:
                logging.info(SwiftVersion()+" Metadata DB: UID=%s Server=%s DB=%s",parms["pyMyUID"],parms["pyMyServer"],parms["pyMyDBase"])
                engine = create_engine('mysql+mysqlconnector://'+parms["pyMyUID"]+':'+
                                       parms["pyMyPwd"]+'@'+parms["pyMyServer"]+'/'+
                                       parms["pyMyDBase"])
            except:
                logging.error(SwiftVersion()+" Error with create engine of type: %s",dbtype)
                return False
            try:
                alccxn = engine.connect()
            except mysql.connector.Error as err:
                logging.error(SwiftVersion()+" Error attempting to create cursor")
                logging.error(SwiftVersion()+' Main error: %s SQLSTATE %s \nDiagnostice message %s',err.errno, err.sqlstate, err.msg)
                return False
            self.metaDB = alccxn
            return True            
        else:
            logging.info(SwiftVersion()+" Error unspecified DB type in call to setupAlchemy")
            return False
        
###############################################################################
        
        
    """ 
    Function: setupFolder
        Makes sure the folder exists where CSV or other files will be created 
        as a result of running the build process.
        Now respects the suffix 'raw' or 'scrambled' to work with Swift and Jay.
        The file path is now constructed as:
            pyLinPath+os.sep+basketID+os.sep+'raw'+os.sep+
        Returns True on success and False on failure
    """   
    def setupFolder(self,parms):
        # Create a folder to save the created files
        try:
            buildfolder = self.path+os.sep+self.bskname+os.sep+parms["pyBuildSuf"]
            scramfolder = self.path+os.sep+self.bskname+os.sep+parms["pyScramSuf"]
            if not os.path.exists(buildfolder):  # Create folder called basketID if it already does not exist.
                logging.info(SwiftVersion()+" Creating directories: %s and %s",buildfolder,scramfolder)
                os.makedirs(buildfolder)
                os.makedirs(scramfolder)
                self.path = buildfolder
                return True
            else:
                logging.info(SwiftVersion()+" Directory %s already exists.",buildfolder)
                self.path = buildfolder
                return True
        except:
            logging.error(SwiftVersion()+" Can't create files/folders in path: %s",buildfolder)
            return False
        
###############################################################################
        
        
        
    """  
    Function: doChecks
         Reads database configuration parameters from a Java like properties file
         and opens connections to MySQL metadata and PostgreSQL data DBs.  Stores
         the connections as object local state variables.  In addition it checks 
         that an appropriate folder exists in the local server file space and 
         creates one if not present.
         
         See Also:
         <ZIPService>, <Boomerang>, <dataExtract>, <dataWrite>
    """          
    def doChecks(self):
        
        # Check correct number of argusments provided when executing the script
        # This version expects arg1 = basketID, arg2 = Swift username
        if len(sys.argv) != 3:
            logging.error(SwiftVersion()+" Arguments should be basket ID and username")
            quit()
            
        # Connect to swift database
        if self.setupAlchemy("metadata",self.props):
            logging.info(SwiftVersion()+" Metadata connection established.")
        else:
            logging.error(SwiftVersion()+" Metadata connection failed.")
            quit()

        # Connect to postgresql data
        if self.setupAlchemy("data",self.props):
            logging.info(SwiftVersion()+ " Data connection established.")
        else:
            logging.error(SwiftVersion()+" Data connection failed.")
            quit()


        # Check basket is present in the swift database
        bskname = sys.argv[1] # The basket name that the script was called with

        # Property: self.bskname
        #    Instance property that stores a reference to the name of the basket supplied as a parameter to this routine
        self.bskname = bskname
        self.setIsa(self.kind(bskname))
        logging.info(SwiftVersion()+" Type: %s; Name: %s",self.getIsa(),self.bskname)
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
        sqlq = text("select * from basketlist where basketID = :bsk union select * from trolleylist where basketID = :bsk")
        Results = self.metaDB.execute(sqlq,bsk=bskname).fetchall()  # Fetches basketID,status, Description,username,createDate
        logging.info(SwiftVersion()+" Search results for basket: %s",Results)
        if not Results and self.getIsa() == "basket":
            logging.error(SwiftVersion()+' This basket does not exist in the Swift database: %s',bskname)
            quit()
        # Check folder can be created or exists
        if not self.setupFolder(self.props):
            logging.error(SwiftVersion()+" Error setting up folder to write files to: %s",self.path)
            quit()



#################################################################################################################################################################################
    """  
    Function: getMetaData
        Queries the MySQL metadata database for information on a basket specified as the local property self.bskname
        It creates *Pandas* data frames with the query results

    """
    def getMetaData(self):
        """
        Property: self.df_varlist
            Instance property that stores a reference to a Pandas data frame of shopping basket elements produced by calling *pd.read_sql* with an SQL query
       About:
            The following query was modified to exclude any variable names with dots in them.  This was causing
            PostgreSQL on Swan to cycle infinitely building the infinite CSV file.  Looks like a buffer overflow problem
            in PostgreSQL 9.6.  PostgreSQL 10 just stops with an error.
        """
        query = "SELECT name FROM shoppingbaskets WHERE basketID = '{0}' AND name NOT LIKE '%.%'".format(self.bskname)
        self.df_varlist = pd.read_sql_query(sql=query, con=self.metaDB)  #Variable list from the shopping basket
        logging.info(SwiftVersion()+" Basket Varlist Query: %s", query)
        logging.info(SwiftVersion()+" Basket Variables:\n %s", self.df_varlist)
        logging.info(SwiftVersion()+" Basket Varlist Dataframe Dims: %s",self.df_varlist.shape)
        # Property: self.df_varsall
        #    Instance property that stores a reference to a Pandas data frame of variable label elements produced by calling *pd.read_sql* with an SQL query
        self.df_varsall = pd.read_sql("SELECT name, label FROM variablelabels",self.metaDB)  
        logging.info(SwiftVersion()+" Name,Label Dataframe Dims: %s",self.df_varsall.shape)
        # Property: self.df_valuesall 
        #    Instance property that stores a reference to a Pandas data frame of value label elements produced by calling *pd.read_sql* with an SQL query
        self.df_valuesall = pd.read_sql("SELECT name, value, label FROM valuelabels",self.metaDB)  
        logging.info(SwiftVersion()+" Value Labels Dataframe Dims: %s",self.df_valuesall.shape)
        # Property: self.df_new
        #    Instance property that stores a reference to a Pandas data frame of merged self.df_varlist and self.df_varsall (variable label) metadata
        self.df_new = pd.merge(self.df_varlist, self.df_varsall,
                               left_on='name',
                               right_on='name',
                               how='inner')
        # About: create the csv file from the above dataframe
        self.df_new.to_csv(self.path+os.sep+'variable_labels_'+self.bskname+'_'+str(self.thetime)+'.csv', index=False, header=['Variable Name', 'Variable Label'])  
        # Property: self.df_new2
        #    Instance property that stores a reference to a Pandas data frame of merged self.df_varlist and self.df_valuessall (value label) metadata
        self.df_new2 = pd.merge(self.df_varlist, self.df_valuesall,
                                left_on='name',
                                right_on='name',
                                how='inner')
        # About: create the csv file from the above dataframe
        self.df_new2.to_csv(self.path+os.sep+'value_labels_'+self.bskname+'_'+str(self.thetime)+'.csv', index=False, header=['Variable Name', 'Value', 'Value Label'])  
        
#################################################################################################################################################################################
    """  Function: getTrolleyMetaData
        Queries the MySQL metadata database for information on a trolley specified as the local property self.bskname
        It creates *Pandas* data frames with the query results

    """
    def getTrolleyMetaData(self):
        from tabulate import tabulate
        # Property: self.df_filelist
        #    Instance property that stores a reference to a Pandas data frame of trolley baket elements produced by calling *pd.read_sql* with an SQL query 
        try:
            query = "SELECT t1.dname,location FROM trollies as t1 LEFT JOIN datasets as t2 ON (t1.dname = t2.dname) WHERE trolleyID = '{0}'".format(self.bskname)
            self.df_filelist = pd.read_sql(sql=query, con=self.metaDB)  #Variable list from the shopping basket
        except:
            logging.error(SwiftVersion()+" Failed to read dataset name and location from DB for trolley: %s.  Quitting.",dset=self.bskname)
            quit()
        else:
            logging.info(tabulate(self.df_filelist,headers='keys',tablefmt='psql'))
  
        
#################################################################################################################################################################################
    """ Function: writeSPSS
          Creates a text file of SPSS syntax that will read a CSV file corresponding to the basket of variables
          specified by self.bskname
    """
    def writeSPSS(self):
        # File: SPSS Script File
        #   Creating the SPSS Script file using the object properties *self.path*, *self.bskname*, and *self.thetime* 
        #    with extension ".sps"
        f = open(self.path+os.sep+'SPSS_Script_'+self.bskname+'_'+str(self.thetime)+'.sps','w+')
        f.write('*************SPSS SCRIPT START************* \n \n \n \n')
        f.write('Note: Use SPSS Windows menu to open the CSV dataset. \n \n')
        f.write('*****SPSS Variable labels***** \n \n')
        for index, row in self.df_new.iterrows():
            if row[0] is not None and row[1] is not None:
                f.write('VARIABLE LABELS' + ' ' + row[0]+ ' ' +"'"+row[1]+"'"+"."+'\n')
        f.write('\n \n \n')
        f.write('*****SPSS Value labels***** \n \n')
        for index, row in self.df_new2.iterrows():
            f.write('ADD VALUE LABELS' + ' ' + row[0] + ' ' +row[1]+' '+"'"+row[2]+"'"+"."+'\n')
        f.write('\n \n \n')
        f.write('*************SPSS SCRIPT END************* \n')
        f.close()

#################################################################################################################################################################################
    """    Function: writeStata
            Creates a text file of Stata syntax that will read a CSV file corresponding to the basket of variables
            specified by self.bskname
    """
    def writeStata(self):
        # File: Stata Script File
        #    Creating the STATA Script file using the object properties *self.path*, *self.bskname*, and *self.thetime* 
        #    with the extension ".do"
        f = open(self.path+os.sep+'STATA_Script_'+self.bskname+'_'+str(self.thetime)+'.do','w+')
        f.write('*************STATA SCRIPT START************* \n \n \n \n')
        f.write('***Location of files*** \n')
        f.write('cd "Please specify the directory filepath for the files" \n \n')
        f.write('***Import dataset*** \n')
        f.write('import delim using'+' '+self.bskname+'_'+str(self.thetime)+'.csv, delim(",") varnames(1) encoding("utf-8") clear \n \n')
        f.write('***STATA Variable labels***** \n \n')
        for index, row in self.df_new.iterrows():
            if row[0] is not None and row[1] is not None:
                f.write('label variable' + ' ' + str.lower(row[0])+ ' ' +'"'+row[1]+'"''\n')
        f.write('\n \n \n')
        f.write('***STATA value labels***** \n \n')
        f.write('**Defining value labels** \n \n')
        for index, row in self.df_new2.iterrows():
            f.write('label define'+' '+str.lower(row[0])+' '+row[1]+' '+'"'+row[2]+'"' +', modify''\n')
        f.write('\n \n \n')
        f.write('**Applying defined labels** \n \n')
        valist = self.df_new2['name'].drop_duplicates().values.tolist()
        for item in valist:
            f.write('label values' + ' ' + str.lower(item) + ' ' + str.lower(item)+'\n')
        f.write('\n \n \n')
        f.write('*************STATA SCRIPT END************* \n')
        f.close()
        #End of STATA Script
#################################################################################################################################################################################
    """    
        Function: thebuilder
           Creates a temporary PostgreSQL table with one field corresponding to the variable name, 
           then creates the metadata files and calls the PostgreSQL function *datawrite* to create
           the data CSV file.  
        See Also:
            <getMetaData>,<writeSPSS>,<writeStata>,<datawrite>
    """
    def thebuilder(self):

        def ExtractAlphanumeric(InputString):
            from string import ascii_letters, digits
            return "".join([ch for ch in InputString if ch in (ascii_letters + digits)])

        self.getMetaData()
        self.writeSPSS()
        self.writeStata()
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
            tabname = str.lower(ExtractAlphanumeric(self.bskname))
            self.df_new.to_sql(name=tabname,
                               con=self.dataDB,
                               index=False,
                               if_exists='replace')
        except:
            logging.error(SwiftVersion()+" Error pushing panda data frame to PG table: %s", tabname)
            quit()
        logging.info(SwiftVersion()+" Created table with variable names:\n %s", self.df_new)
        # note table name is turned to lower case as the cur.execute command is looking for a lower case table name.
        # changing column type for varname to varchar from text. Is required for the postgres function extractdata and writedata
        # Notice the wierd requirement to embed the sql in 3 consequtive double quote marks.
        try:
            self.dataDB.execute("""ALTER TABLE public.{table} ALTER COLUMN varname TYPE varchar""".format(table=tabname))
        except psycopg2.Error as e:
            logging.error(SwiftVersion()+" Error attempting to ALTER TABLE: %s\n", tabname)
            logging.error(SwiftVersion()+' Main error: %s on cursor %s \nDiagnostic message %s',e.pgerror,e.diag.message_primary,e.cursor)
            quit()

        #note table name is turned to lower case as the cur.execute command is looking for a lower case table name.

        file = self.path+os.sep+self.bskname+'_'+str(self.thetime)+'.csv'
        #run the extractdata function in postgres see http://initd.org/psycopg/docs/usage.html#query-parameters
        try:
            # run the writedata function in postgres
            logging.info(SwiftVersion()+" Calling psql datawrite with table: %s and file: %s",tabname,file)
            self.dataDB.execute("""select datawrite(%s,%s)""",
                                (tabname, file))

        except psycopg2.Error as e:
            logging.error(SwiftVersion()+" Error calling datawrite: %s", e.pgerror)
            quit()
        # clean up the table created via to_sql above
        try:
            #logging.info(SwiftVersion()+" Suspending clean up at the moment; table: %s not cleard",tabname)
            self.dataDB.execute("""DROP TABLE public.{table}""".format(table=tabname))
        except psycopg2.Error as e:
            logging.error(SwiftVersion()+" Error cleaning up table %s, but not quitting.\n", tabname)
            
#################################################################################################################################################################################
    """    
        Function: getscoop
           Looks for a list of files and scoops them up into a zip file.
           Doesn't use the DB at the moment  
        See Also:

    """
    def getscoop(self):
        import shutil
        self.getTrolleyMetaData()
        pathtofolder = self.path+os.sep
        for row  in self.df_filelist.itertuples():
            try:
                shutil.copy2(getattr(row,'location'),pathtofolder)
            except:
                logging.error(SwiftVersion()+" Failed to copy file from %s to %s, but not quitting.",
                              location,
                              pathtofolder)

            
        


#################################################################################################################################################################################
#    Function: main
#        When called as a stand alone program this creates an instance called *bob* 
#        and calls *thebuilder* method
#
#    Parameters:
#        - basket name
#        - Swift user name of basket owner
if __name__ == "__main__":
    bob = BasketBuilder()
    logging.info(SwiftVersion()+' Build Swift Basket {arg} from PostgreSQL NSHD Repository:'.format(arg=sys.argv[1]))
    logging.info(SwiftVersion()+' Files will be created with the time stamp of: {tim}'.format(tim=bob.thetime))
    if bob.getIsa() == "basket":
        if bob.dataExists():
            # Nothing to do already built
            pass
        else:
            # Need to build it
            bob.thebuilder()
    elif bob.getIsa() == "trolley":
        bob.getscoop()
    logging.info(SwiftVersion()+' Finished.')
###############################################################################
