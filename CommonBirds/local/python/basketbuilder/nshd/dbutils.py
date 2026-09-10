# import main components
import os
import mysql.connector
from mysql.connector import Error,ProgrammingError,OperationalError,IntegrityError,InternalError
import psycopg2
from sqlalchemy import create_engine
from sqlalchemy.sql import select,text
from tabulate import tabulate
import pandas as pd
# import package components
from . commondefs import *
from . fileutils import FileOps

_DATA_FILE_STEM = "data_"
_DICTIONARY_STEM = "variable_labels_data_dictionary_"
_VALUE_LABELS_STEM = "value_labels_"
_DEFAULT_TYPE = "metadata"

class DBUtils:

    def __init__(self, dbtype=_DEFAULT_TYPE):
        """! @brief Constructor for DBUtils class """
        super().__init__()  # This now initializes the shared state
        self.parms = self.get_props()
        self.df_varsall = None # variable labels for varlist
        self.df_valuesall = None # value labels for varlist
        self.dbcnxn = None
        self.dbtype = dbtype
        dbcnxn = self.setupAlchemy()
        if dbtype == "data":
            self.set_datadbase(dbcnxn)
        elif dbtype == "metadata":
            self.set_metadbase(dbcnxn)
        elif dbtype == "scramble":
            self.set_scrmdbase(dbcnxn)
        else:
            raise DBaseError("Unknown DB type", "Unknown DB")
            

    ###############################################################################

    def setupAlchemy(self):
        """! @brief Makes use of the sqlalchemy package to create db connections
            to either PostgreSQL data or MySQL metadata databases.
        
            As as side effect assigns connections to instance properties metaDB or dataDB.
            Pandas operations on DB need SQLAlchemy connections as the DB interface.
            
        """
        self.debug_log(f"{self.ThisVersion()} setupAlchemy Type: {self.dbtype}")
        if self.dbtype == "data":
            return self.data_connection()
        elif self.dbtype == "metadata":
            return self.metadata_connection()
        elif self.dbtype == "scramble":
            return self.scramble_connection()
        else:
            self.info_log(f"{self.ThisVersion()} Error unspecified DB type in {self.dbtype} call to setupAlchemy")
            raise DBaseError(f"Type of database not recognised ({self.dbtype}) in connection attempt")
        
    ###############################################################################

    def data_connection(self):
        # create PostgreSQL engine with parameters
        try:
            conn_string = 'postgresql+psycopg2://' + \
                self.parms["pyPgUID"] + ':' + self.parms["pyPgPwd"] + '@' + \
                self.parms["pyPgServer"] + '/' + self.parms["pyPgDBase"]
            info_log(f"{ThisVersion()} {self.dbtype} DB: UID={self.parms['pyPgUID']} Server={self.parms['pyPgServer']} DB={self.parms['pyPgDBase']}")
            engine = create_engine(conn_string)
        except Exception as err:
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error {err} with create engine using: {conn_string}")
            raise DataOpenError(f"Can't connect using {conn_string}") 
        # Now create an actual DB connection from the engine
        try:
            alccxn = engine.connect()
        except psycopg2.Error as err:
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error attempting to create cursor")
            self.error_log(f"{self.self.ThisVersion()} {self.dbtype} DB: Main error: {err.pgerror} on cursor {err.cursor} Diagnostice message {err.diag.message_primary}")
            raise  DataOpenError(f"{self.dbtype} DB: Can't get cursor for connection {err.diag.message_primary}")
        self.dbcnxn = alccxn
        self.debug_log(f"{self.ThisVersion()} setupAlchemy Successful for {self.dbtype}")
        self.set_datadbase(self.dbcnxn)
        return self.dbcnxn


    ###############################################################################

    def metadata_connection(self):
        # create MySQL engine with parameters
        try:
            conn_string = 'mysql+mysqlconnector://' + self.parms["pyMyUID"] + ':' + \
                self.parms["pyMyPwd"] + '@' + self.parms["pyMyServer"] +'/' + \
                self.parms["pyMyDBase"]
            self.info_log(f"{self.ThisVersion()} {self.dbtype} DB: UID={self.parms['pyMyUID']} Server={self.parms['pyMyServer']} DB={self.parms['pyMyDBase']}")
            engine = create_engine(conn_string)
        except Error as err:
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Engine failure using: {conn_string}")
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error code: {err.errno} SQLSTATE: {err.sqlstate} Message: {err.msg}")
            raise MetaOpenError(f"{self.dbtype} DB: Can't connect to {self.parms['pyMyDBase']}")
        # Now create an actual DB connection from the engine
        try:
            alccxn = engine.connect()
        except OperationalError as err:
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Connection timeout or failure, can't connect using: {conn_string}")
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error code: {err.errno} SQLSTATE: {err.sqlstate} Message: {err.msg}")
            raise MetaOpenError(f"{self.dbtype} DB: Can't connect to {self.parms['pyMyDBase']}")
        except ProgrammingError as err:
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: SQLSyntax or bad API usage, can't connect using: {conn_string}")
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error code: {err.errno} SQLSTATE: {err.sqlstate} Message: {err.msg}")
            raise MetaOpenError(f"{self.dbtype} DB: Can't connect to {self.parms['pyMyDBase']}")
        except InternalError as err:
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Server internal issue, can't connect using: {conn_string}")
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error code: {err.errno} SQLSTATE: {err.sqlstate} Message: {err.msg}")
            raise MetaOpenError(f"{self.dbtype} DB: Can't connect to {self.parms['pyMyDBase']}")            
        except mysql.connector.Error as err:
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error attempting to create cursor")
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error code: {err.errno} SQLSTATE: {err.sqlstate} Message: {err.msg}")
            raise MetaOpenError(f"{self.dbtype} DB: Can't get cursor for connection to {self.parms['pyMyDBase']}")
        self.dbcnxn = alccxn
        self.debug_log(f"{self.ThisVersion()} setupAlchemy Successful for {self.dbtype}")
        self.set_metadbase(self.dbcnxn)
        return self.dbcnxn     
        
    ###############################################################################

    def scramble_connection(self):
        # create MySQL engine with parameters
        try:
            conn_string = 'mysql+mysqlconnector://' + self.parms["pyMyUID"] + ':' + \
                self.parms["pyMyPwd"] + '@' + self.parms["pyMyServer"] +'/' + \
                self.parms["pyMyDBaseScram"]
            self.info_log(f"{self.ThisVersion()} {self.dbtype} DB: UID={self.parms['pyMyUID']} Server={self.parms['pyMyServer']} DB={self.parms['pyMyDBaseScram']}")
            engine = create_engine(conn_string)
        except Error as err:
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: engine failure using: {conn_string}")
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error code: {err.errno} SQLSTATE: {err.sqlstate} Message: {err.msg}")
            raise MetaOpenError(f"{self.dbtype} DB: Can't connect to {self.parms['pyMyDBaseScram']}")
        # Now create an actual DB connection from the engine
        try:
            alccxn = engine.connect()
        except OperationalError as err:
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Connection timeout or failure, can't connect using: {conn_string}")
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error code: {err.errno} SQLSTATE: {err.sqlstate} Message: {err.msg}")
            raise MetaOpenError(f"{self.dbtype} DB: Can't connect to {self.parms['pyMyDBaseScram']}")
        except ProgrammingError as err:
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: SQLSyntax or bad API usage, can't connect using: {conn_string}")
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error code: {err.errno} SQLSTATE: {err.sqlstate} Message: {err.msg}")
            raise MetaOpenError(f"{self.dbtype} DB: Can't connect to {self.parms['pyMyDBaseScram']}")
        except InternalError as err:
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Server internal issue, can't connect using: {conn_string}")
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error code: {err.errno} SQLSTATE: {err.sqlstate} Message: {err.msg}")
            raise MetaOpenError(f"{self.dbtype} DB: Can't connect to {self.parms['pyMyDBaseScram']}")            
        except Error as err:
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error can't connect using: {conn_string}")
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error code: {err.errno} SQLSTATE: {err.sqlstate} Message: {err.msg}")
            raise MetaOpenError(f"{self.dbtype} DB: Can't connect to {self.parms['pyMyDBaseScram']}")
        except mysql.connector.Error as err:
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error attempting to create cursor")
            self.error_log(f"{self.ThisVersion()} {self.dbtype} DB: Error code: {err.errno} SQLSTATE: {err.sqlstate} Message: {err.msg}")
            raise MetaOpenError(f"{self.dbtype} DB: Can't get cursor for connection to {self.parms['pyMyDBaseScram']}")
        self.dbcnxn = alccxn
        self.debug_log(f"{self.ThisVersion()} setupAlchemy Successful for {self.dbtype}")
        self.set_scrmdbase(self.dbcnxn)
        return self.dbcnxn     
        
    ###############################################################################

    def remove_serno_add_ntag1(self,df):
        """! @brief Takes a list of variables in a data frame and substitutes ntag1 for serno
        @param dframe - Pandas dataframe
        @returns DataFrame without serno but including ntag1
        """
        # First remove serno
        df = df[~df['name'].str.contains('serno', case=False)]
        # Create extra_var DataFrame with just ntag1
        extra_var = pd.DataFrame( {'name': ['ntag1']} )
        # Check if 'ntag1' exists in the 'name' column
        has_ntag1 = df['name'].str.contains('ntag1', na=False).any()
        if not has_ntag1:
            # Concatenate
            df = pd.concat([df, extra_var], ignore_index=True)
        return df

        ###############################################################################
    def getMetaData(self):
        """! @brief Queries the MySQL metadata database for information on a specified basket.

        It creates *Pandas* data frames with the query results and writes CSV files with metadata.
        Producing the CSV files with metadata is the main point of this method.

        @returns Nothing

        """
        """
        Property: self.df_varlist
            Instance property that stores a reference to a Pandas data frame of shopping basket elements produced by calling
            *pd.read_sql* with an SQL query
       About:
            The following query was modified to exclude any variable names with dots in them.  This was causing
            PostgreSQL on Swan to cycle infinitely building the infinite CSV file.  Looks like a buffer overflow problem
            in PostgreSQL 9.6.  PostgreSQL 10 just stops with an error.
        """
        self.debug_log(f"{self.ThisVersion()} getMetaData: attempting to generate metadata for {get_bskname()}")
        fops = FileOps()
        # Property: self.df_new
        #    Instance property that stores a reference to a Pandas data frame of  (variable label) metadata
        query = """
        with
          get_BasketVars AS (select Name from rook.shoppingbaskets where basketId = '{0}' and Name NOT LIKE '%.%')
        select Name, Label from rook.variablelabels where Name in (select Name from get_BasketVars);
        """.format(self.get_bskname())
        try:
            self.get_metadbase().begin()
            df_new = pd.read_sql_query(query, self.get_metadbase())
            self.get_metadbase().commit()
        except:
            raise MetaAccessError(f"Can't get dataframe from query: {query}")
        # Change all column names to lower case
        df_new.columns = df_new.columns.str.lower()
        self.debug_log(f"{self.ThisVersion()} getMetaData: Data Dictionary DF: {df_new}")
        self.info_log(f"{self.ThisVersion()} getMetaData: basket data dictionary dims: {df_new.shape} columns: {df_new.columns}")
        data_dict_file_name = _DICTIONARY_STEM + self.get_bskname() + '_' + str(self.get_thetime()) + '.csv'
        # store the result in CommonDefs
        self.set_data_dict(df_new)
        # About: create the csv data dictionary file from the above dataframe
        try:
            df_new.to_csv(fops.path+os.sep+data_dict_file_name,
                          index=False, header=['Variable_Name', 'Variable_Label'])
        except (IOError, OSError) as err:
            self.error_log(f"{self.ThisVersion()} getMetaData error trying to write data dictionary file: {err}")
        # Property: self.df_new2
        #    Instance property that stores a reference to a Pandas data frame of merged self.df_varlist and self.df_valuessall (value label) metadata
        query = """
        with
          get_BasketVars AS (select Name from rook.shoppingbaskets where basketId = '{0}' and Name NOT LIKE '%.%')
        select Name, Value, Label from rook.valuelabels where Name in (select Name from get_BasketVars);
        """.format(self.get_bskname())
        try:
            self.get_metadbase().begin()
            df_new2 = pd.read_sql_query(query, self.get_metadbase())
            self.get_metadbase().commit()
        except:
            raise MetaAccessError(f"Can't get dataframe from query: {query}")
        # Change all the column names to lower case
        df_new2.columns = df_new2.columns.str.lower()
        self.debug_log(f"{self.ThisVersion()} getMetaData: Value Labels DF: {df_new2}")
        self.info_log(f"{self.ThisVersion()} getMetaData: basket value labels dims: {df_new2.shape} columns: {df_new2.columns}")
        value_labels_file_name = _VALUE_LABELS_STEM + self.get_bskname() + '_' + str(self.get_thetime()) + '.csv'
        # store the result in CommonDefs
        self.set_value_labels(df_new2)
        # About: create the csv file from the above dataframe
        try:
            df_new2.to_csv(fops.path+os.sep+value_labels_file_name,
                           index=False, header=['Variable_Name', 'Value', 'Value_Label'])
        except (IOError, OSError) as err:
            self.error_log(f"{self.ThisVersion()} getMetaData error trying to write value labels file: {err}")
            
    ###############################################################################################################

    def getTrolleyMetaData(self):
        """! @brief Queries the MySQL metadata database for information on a trolley
        specified as the local property get_bskname()

        @returns Pandas data frames with the query results

        """
        # Property: self.df_filelist
        #    Instance property that stores a reference to a Pandas data frame of trolley baket elements produced by calling *pd.read_sql* with an SQL query 
        self.debug_log(f"{self.ThisVersion()} getTrolleyMetaData: attempting to generate metadata for {get_bskname()}")
        try:
            query = f"SELECT t1.dname,location FROM trollies as t1 LEFT JOIN datasets as t2 ON (t1.dname = t2.dname) WHERE trolleyID = '{self.get_bskname()}'"
            df_filelist = pd.read_sql(sql=query, con=get_metadbase())  #Variable list from the shopping basket
        except:
            self.error_log(f"{self.ThisVersion()} Failed to read dataset name and location from DB for trolley: {self.get_bskname()}.  Quitting.")
            raise DBaseError(f"Can't get dataframe from query: {query}")
        else:
            self.info_log(f"{self.ThisVersion()} getTrolleyMetadata: {tabulate(df_filelist,headers='keys',tablefmt='psql')}")
            self.set_trolley_dframe(df_filelist)
        
    ###############################################################################################################

    def userCheck(self):
        # Check user exists in the users table in the scrambling database
        sqlq = text("select * from users where user  = '{name}'".format(name=self.get_usrname()))
        self.debug_log(f"{self.ThisVersion()} DBUtils:userCheck: Scrambling DB connection has type {type(self.get_scrmdbase())}")
        try:
            self.get_scrmdbase().begin()
            results = self.get_scrmdbase().execute(sqlq).fetchall()  # Fetches basketID,Description,username,createDate
            self.get_scrmdbase().commit()
        except Exception as err:
            self.debug_log(f"{self.ThisVersion()} DBUtils:userCheck ERROR {err}")
            raise MetaAccessError(f"userCheck: failed to execute query {sqlq}")
        if not results:
            self.error_log(f"{self.ThisVersion()} DBUtils:userCheck: Cannot find user: {self.get_username()}  in scrambling DB: ")
            return False
        else:
            return True

    ###############################################################################################################

    def read_seq(self):
        myquery = text(f"SELECT * FROM users WHERE user = '{self.get_username()}'")  # The name of our query
        try:
            self.get_scrmdbase().begin()
            Results = self.get_scrmdbase().execute(myquery).fetchall()  # Fetches all the data
            self.get_scrmdbase().commit()
        except:
            raise MetaAccessError(f"DBUtils:read_seq: failed to execute query {myquery}")
        for Result in Results:
            User = Result[0]
            ProjectID = Result[1]
            Seqtable = Result[2]
            Colname = Result[3]
        query = text(f"SELECT serno, {Colname} FROM {Seqtable} ORDER BY serno asc")
        try:
            self.get_scrmdbase().begin()
            dfSeq = pd.read_sql_query(query, self.get_scrmdbase())
            self.get_scrmdbase().commit()
        except:
            raise MetaAccessError(f"read_seq: Can't get dataframe from query: {query}")
        dfSeq = dfSeq.rename(columns={Colname: 'nshdid_' + self.get_usrname()})
        dfSeq.serno = dfSeq.serno.astype(float)
        self.set_seq(dfSeq) # Store DF
        self.debug_log(f"{self.ThisVersion()} Stored sequence {dfSeq.head()}")
        # Read the ntags file only if file not already sernoed
        if get_identifier() != 'serno':
            query = text(f"SELECT serno, {self.get_identifier()} FROM {newnshdid} ORDER BY {self.get_identifier()} asc")
            try:
                self.get_scrmdbase().begin()
                dfNtag = pd.read_sql_query(query, self.get_scrmdbase())
                self.get_scrmdbase().commit()
            except:
                raise MetaAccessError(f"read_seq: Can't get dataframe from query: {query}")
        elif get_identifier() == 'serno':
            query = text(f"SELECT {self.get_identifier()} FROM {newnshdid} ORDER BY {self.get_identifier()} asc")
            try:
                self.get_scrmdbase().begin()
                dfNtag = pd.read_sql_query(query, self.get_scrmdbase())
                self.get_scrmdbase().commit()
            except:
                raise MetaAccessError(f"read_seq: Can't get dataframe from query: {query}")
        self.set_ntags(dfNtag)
        self.debug_log(f"{self.ThisVersion()} Stored NTAGs {dfNtag.head()}")
        

    #####################################################################################
    

    def record_scramble(self):
        src_file = self.get_raw_file()
        clsfld = "scramblebasket command"
        # Updated for SQLAlchemy version 2.0
        with self.get_scrmdbase().begin() as conx:
            # Logging process into the scrambling database
            # Fields in DB:
            # id (auto increment primary key)
            # timepoint DATETIME
            # class VARCHAR (256)
            # input VARCHAR (256) NULLABLE
            # output VARCHAR (256) NULLABLE
            # scramuser VARCHAR (256) NULLABLE
            # swiftproject VARCHAR (256) NULLABLE
            sql = text(f"INSERT INTO history_copy (timepoint, class, input, output, scramuser) VALUES (NOW(),'{clsfld}','{src_file}','{self.get_scram_file()}','{self.get_usrname()}')")
            try:
                # Execute the SQL command
                self.get_scrmdbase().execute(sql)
                # Commit your changes in the database
                self.get_scrmdbase().commit()
            except Exception as err:
                # Rollback is taken care of by context mgr in case there is an error
                self.error_log(f"{self.ThisVersion()} Scramble:record_scramble: ERROR {err} **** Rolling back to before INSERT")


    ###################################### End #########################################
        
