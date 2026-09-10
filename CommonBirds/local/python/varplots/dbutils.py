import psycopg2
import pandas as pd
import numpy as np
import sqlalchemy as sq
import pymysql
import mysql.connector as myc
from mysql.connector import errorcode
import commondefs as cd




class DB_Utils():
    """! @brief Encapsulate details of opening databases
    """
    def __init__(self, propdict, source=None):
        """! @brief Constructor - Initialises instance attributes 
        and checks properties 
        @param propdict - dictionary object derived from YAML file
        @param source - string argument with csv: or table: prefix
        @details
         Properties: 
           datacnxn - instance property: connection object for data
           datacrsr - instance property: cursor from existing data connection
           metacnxn - instance property: connection object for metadata
           mysqcnxn - instance property: connection object from mysql.connector
           source   - instance property: data from file or table encoded here
        """
        self.metacnxn  = None
        self.datacnxn  = None
        self.mysqlpool = None
        self.source    = source
        if isinstance(propdict,dict):
           self.parms = propdict
        else:
            sys.exit("DB_Utils: No valid dictionary of properties")
        if cd.SourceData.isrdb:
            self.createPool() # This will assign to self.mysqlpool
            self.metaDB()
            self.dataDB()
        else:
            print(f"DB_Utils.__init__ : Failure to connect to source data and/or metadata. {cd.SourceData}")
        
    #-------------------------------------------------------------------------#

    def getDconx(self):
        """! @brief  Return the data connection instance variable value
        """
        return self.datacnxn

    #-------------------------------------------------------------------------#
    def getMconx(self):
        """! @brief Return the metadata connection instance
        """
        return self.metacnxn

    #-------------------------------------------------------------------------#
    def getMySQLconx(self):
        """! @brief Return the mysql.connector connection
        """
        return self.mysqlpool
    
    #-------------------------------------------------------------------------#

    def metaDB(self):
        """! Makes use of the mysql.connector package to create 
        db connections to MySQL databases.
        As as side effect assigns connections to instance property metaDB

        @returns boolean True on success or False on failure
        """
        # create MySQL connection with parameters
        try:
            connstring = "mysql+pymysql://"+self.parms["MyUID"] + ":" + \
            self.parms["MyPwd"] + "@" + self.parms["MyServer"] +"/" + self.parms["MyDBase"]
            eng = sq.create_engine(connstring) # Returns SQLAlchemy "engine"
        except mysql.connector.Error as err:
            if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
                cd.error_log("MySQL Error: Bad user name or password")
            elif err.errno == errorcode.ER_BAD_DB_ERROR:
                 cd.error_log(f"MySQL Error: Database does not exist: {self.parms['MyDBase']}")
            else:
                cd.error_log(f"MySQL Error: {err}")
            return False
        self.metacnxn = eng
        return True

    #-------------------------------------------------------------------------#

    def dataDB(self):
        """! @brief Makes use of the psycopg2 package 
        to create db connections to PostgreSQL databases.
        As as side effect assigns connections to instance 
        property dataDB
        
        @returns boolean True on success or False on failure
        """
        # create PostgreSQL connection with parameters
        # SQLAlchemy accepts psycopg2 args in the form:
        # postgresql+psycopg2://user:password@host:port/dbname[?key=value&key=value...]
        try:
            connstring = "postgresql+psycopg2://"+self.parms["PgUID"] + ":" + \
            self.parms["PgPwd"] + "@" + self.parms["PgServer"] +"/" + self.parms["PgDBase"]
            conn = sq.create_engine(connstring)
        except psycopg2.Error as err:
            cd.info_log(f"Psycopg2 Error: {err.pgerror}")
            cd.info_log(f"Error with DB connection to: {self.parms['PgDBase']}")
            cd.info_log(f"Connection Parameter Server: {self.parms['PgServer']}")
            return False
        self.datacnxn = conn
        return True

#=============================================================================#

    def createPool(self):
        """
        """
        cd.info_log(f"DB_Utils.createPool : creating MySQL connection to {self.parms['MyServer']}")
        try:
            conn = myc.pooling.MySQLConnectionPool(pool_name="varplots",
                                                   pool_size=32,
                                                   autocommit=True,
                                                   host=self.parms["MyServer"],
                                                   user=self.parms["MyUID"],
                                                   password=self.parms["MyPwd"],
                                                   database=self.parms["MyDBase"])
        except myc.Error as err:
            if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
                cd.error_log(f"Error DBUtils.MycCon : ({str(err)}): Wrong user name or password")
            elif err.errno == errorcode.ER_BAD_DB_ERROR:
                cd.error_log(f"Error DBUtils.MycCon : ({str(err)}): Database does not exist")
            else:
                cd.error_log(f"Error DBUtils.MycCon : {str(err)}")
                sys.exit(f"Error DBUtils.MycCon {str(err)}")
            return False
        cd.info_log(f"DBUtils.mycCon: Connection for {self.parms['MyDBase']} created")
        self.mysqlpool = conn
        return True
