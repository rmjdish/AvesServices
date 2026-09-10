import sys
import pandas as pd
import datetime
from . dbutils import DB_Utils
from . import commondefs as cd

__DEFAULT_YAML = """---
# Connections to election data
COMMONDEFS:
  - PgServer: donkey.local
    PgUID: postgres
    PgPwd: m1a9r1c5h
    PgDBase: genelects
    Schm1997: ge1997
    Schm2001: ge2001
    Schm2005: ge2005
    Schm2010: ge2010
    Schm2015: ge2015
    Schm2017: ge2017
    Schm2019: ge2019
    Schm2024: ge2024
    SchmBrex: brexit
    Tabl1997: ge1997
    Tabl2001: ge2001
    Tabl2005: ge2005
    Tabl2010: ge2010
    Tabl2015: ge2015
    Tabl2017: ge2017
    Tabl2019: er_2019
    Tabl2024: ge2024constits
    Tabl1997meta: ge1997meta
    Tabl2001meta: ge2001meta
    Tabl2005meta: ge2005meta
    Tabl2010meta: ge2010meta
    Tabl2015meta: ge2015meta
    Tabl2017meta: ge2017meta
    Tabl2019meta: ge2019meta
    Tabl2024meta: ge2024meta
    TableBrexit: euref_cons
"""

def create_yaml_file():
    with open("commondefs.yaml",mode="w",encoding="utf8") as f:
        f.writelines(__DEFAULT_YAML)


class DataPull():
    """! @brief Gets data from data and metadata databases and 
    returns it as Pandas dataframes through pull methods
    """
 
    def __init__(self):
        """! @brief Constructor
        Initialises instance objects with parameters derived from YAML file
        
        @param propdict - Dictionary of properties derived from YAML file
        """
        p = cd.CommonDefs() # Instantiation triggers readprops
        self.schema = None
        self.table  = None
        if isinstance(cd.get_props(), dict):
            self.parms = cd.get_props()
        else:
            sys.exit("DataPull: No valid dictionary of properties")


    #-------------------------------------------------------------------------#

    def setSchema(self, schema):
        """! @brief assigns parameter schema to instance property self.schema 
        """
        if isinstance(schema, str):
            self.schema = schema
            
    #-------------------------------------------------------------------------#

    def setTable(self, table):
        """! @brief assigns parameter table to instance property self.table 
        """
        if isinstance(table, str):
            self.table = table
            
    #-------------------------------------------------------------------------#

    def getPG(self):
        """! @brief Read all data from a PostgreSQL table and assign to 
        instance property dataframe
        @returns Pandas dataframe corresponding to table
        """
        query = "select * from " + self.schema + "." + self.table + ";"
        ts = datetime.datetime.now()
        cd.info_log(f"{ts.strftime('%c')} getPG: Querying DB with: {query}") 
        try:
            dbpg = DB_Utils(self.parms)
            with dbpg.getDconx().connect() as cnx:
                df = pd.read_sql_query(query, cnx)
        except Exception as err:
            cd.error_log(f"{ts.strftime('%c')} getPG: Error executing query: {str(err)}")
            sys.exit("getPG: "+str(err))
        return df

    #-------------------------------------------------------------------------#
    def pullBrexit(self):
        """! @brief Read 2016 EU referendum data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["SchmBrex"]
        self.table = self.parms["TableBrexit"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE2024(self):
        """! @brief Read 2024 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm2024"]
        self.table = self.parms["Tabl2024"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE2019(self):
        """! @brief Read 2019 UK GE data from a PostgreSQL table and
        assign to instance property dataframe @returns Pandas
        dataframe

        """
        self.schema = self.parms["Schm2019"]
        self.table = self.parms["Tabl2019"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE2017(self):
        """! @brief Read 2024 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm2017"]
        self.table = self.parms["Tabl2017"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE2015(self):
        """! @brief Read 2015 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm2015"]
        self.table = self.parms["Tabl2015"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE2010(self):
        """! @brief Read 2010 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm2010"]
        self.table = self.parms["Tabl2010"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE2005(self):
        """! @brief Read 2005 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm2005"]
        self.table = self.parms["Tabl2005"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE2001(self):
        """! @brief Read 2001 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm2001"]
        self.table = self.parms["Tabl2001"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE1997(self):
        """! @brief Read 1997 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm1997"]
        self.table = self.parms["Tabl1997"]
        return self.getPG()

    #-------------------------------------------------------------------------#


    #-------------------------------------------------------------------------#
    def pullGE2024meta(self):
        """! @brief Read 2024 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm2024"]
        self.table = self.parms["Tabl2024meta"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE2019meta(self):
        """! @brief Read 2019 UK GE data from a PostgreSQL table and
        assign to instance property dataframe @returns Pandas
        dataframe

        """
        self.schema = self.parms["Schm2019"]
        self.table = self.parms["Tabl2019meta"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE2017meta(self):
        """! @brief Read 2024 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm2017"]
        self.table = self.parms["Tabl2017meta"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE2015meta(self):
        """! @brief Read 2015 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm2015"]
        self.table = self.parms["Tabl2015meta"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE2010meta(self):
        """! @brief Read 2010 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm2010"]
        self.table = self.parms["Tabl2010meta"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE2005meta(self):
        """! @brief Read 2005 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm2005"]
        self.table = self.parms["Tabl2005meta"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE2001meta(self):
        """! @brief Read 2001 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm2001"]
        self.table = self.parms["Tabl2001meta"]
        return self.getPG()

    #-------------------------------------------------------------------------#
    def pullGE1997meta(self):
        """! @brief Read 1997 GE data from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        self.schema = self.parms["Schm1997"]
        self.table = self.parms["Tabl1997meta"]
        return self.getPG()


    #-------------------------------------------------------------------------#
    

    
