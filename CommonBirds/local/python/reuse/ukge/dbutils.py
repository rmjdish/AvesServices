import psycopg2
import pandas as pd
import numpy as np
import sqlalchemy as sq
from . import commondefs as cd
from ruamel.yaml import YAML

class DB_Utils():
    """! @brief Encapsulate details of opening databases

    This version is simplified and only works with Postgresql databases
    """
    def __init__(self, propdict):
        """! @brief Constructor - Initialises instance attributes 
        and checks properties 
        @param propdict - dictionary object derived from YAML file
        @details
         Properties: 
           datacnxn - instance property: connection object for data
           datacrsr - instance property: cursor from existing data connection
           metacnxn - instance property: connection objcet for metadata
        """
        self.datacnxn = None
        self.datacrsr = None
        self.metacnxn = None
        if isinstance(propdict,dict):
           self.parms = propdict
        else:
            sys.exit("DB_Utils: No valid dictionary of properties")
        self.dataDB()
        
    #-------------------------------------------------------------------------#

    def getDconx(self):
        """! @brief  Return the data connection instance variable value
        """
        return self.datacnxn

    #-------------------------------------------------------------------------#
    def getMconx(self):
        """! Return the metadata connection instance
        """
        return self.metacnxn

    #-------------------------------------------------------------------------#

    def metaDB(self):
        """! Makes use of the psycopg2 package to create db
        connections to PostgreSQL databases.  As as side effect
        assigns connections to instance property metaDB

        @returns boolean True on success or False on failure

        """
        # create MySQL connection with parameters
        try:
            connstring = "postgresql+psycopg2://"+self.parms["PgUID"] + ":" + \
            self.parms["PgPwd"] + "@" + self.parms["PgServer"] +"/" \
            + self.parms["PgDBase"]
            conn = sq.create_engine(connstring)
        except psycopg2.Error as err:
            cd.info_log(f"Psycopg2 Error: {err.pgerror}")
            cd.info_log(f"Error with DB connection to: {self.parms['PgDBase']}")
            cd.info_log(f"Connection Parameter Server: {self.parms['PgServer']}")
            return False
        self.metacnxn = conn
        return True

    #-------------------------------------------------------------------------#

    def dataDB(self):
        """! @brief Makes use of the psycopg2 package 
        to create db connections to PostgreSQL databases.
        As a side effect assigns connections to instance 
        property dataDB
        
        @returns boolean True on success or False on failure
        """
        # create PostgreSQL connection with parameters
        # SQLAlchemy accepts psycopg2 args in the form:
        # postgresql+psycopg2://user:password@host:port/dbname[?key=value&key=value...]
        try:
            connstring = "postgresql+psycopg2://"+self.parms["PgUID"] + ":" + \
                self.parms["PgPwd"] + "@" + self.parms["PgServer"] + \
                "/" + self.parms["PgDBase"]
            conn = sq.create_engine(connstring)
        except psycopg2.Error as err:
            cd.info_log(f"Psycopg2 Error: {err.pgerror}")
            cd.info_log(f"Error with DB connection to: {self.parms['PgDBase']}")
            cd.info_log(f"Connection Parameter Server: {self.parms['PgServer']}")
            return False
        self.datacnxn = conn
        return True

 
#=============================================================================#
