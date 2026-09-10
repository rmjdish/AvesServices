import sys
import pandas as pd
from dbutils import DB_Utils
import mysql.connector as myc
from mysql.connector import errorcode
import commondefs as cd


class DataPull():
    """! @brief Gets data from data and metadata databases and 
    returns it as Pandas dataframes through pull methods
    """
 
    def __init__(self):
        """! @brief Constructor
        Initialises instance objects with parameters derived from YAML file
        
        @param propdict - Dictionary of properties derived from YAML file
        """
        self.schema = cd.get_schema()
        self.table  = cd.SourceData.table
        self.fname  = cd.SourceData.file
        if isinstance(cd.get_props(), dict):
            self.parms = cd.get_props()
            cd.info_log(f"DataPull.__init__ : creating DB_Utils instance")
            self.dbut   = DB_Utils(self.parms) # Get a handle on DB_Utils lower layer
        else:
            sys.exit("DataPull: No valid dictionary of properties")


    #-------------------------------------------------------------------------#
    def pullData(self):
        """! @brief Read in data from correct source and assign to 
        instance property dataframe
        @returns Pandas DataFrame
        @details See Also: <getPG> <getCSV>
        """
        print("SourceData: ", cd.SourceData)
        if cd.SourceData.isrdb:
            return self.getPG()
        else:
            return self.getCSV(cd.SourceData.file)

    #-------------------------------------------------------------------------#
    def getCSV(self):
        """! @brief Read in data from a CSV file and assign to 
        instance property dataframe
        @param filename - String path to CSV file
        @returns Pandas dataframe
        """
        if self.fname is None:
            df = pd.DataFrame() #Empty DataFrame
        else:
            try:
                df = pd.read_csv(self.fname)
            except:
                sys.exit('Error reading CSV file: ' + self.fname)
        return df

    #-------------------------------------------------------------------------#
    def getPG(self):
        """! @brief Read all data from a PostgreSQL table and assign to 
        instance property dataframe
        @param schname - String database schema containing table
        @param tabname - String tablename containing variables
        @returns Pandas dataframe corresponding to table
        """
        query = "select * from " + self.schema + "." + self.table + ";"
        cd.info_log(f"getPG: Querying DB with: {query}") 
        try:
            dbpg = self.dbut
            with dbpg.getDconx().connect() as cnx:
                df = pd.read_sql_query(query, cnx)
        except Exception as err:
            cd.error_log(f"getPG: Error executing query: {str(err)}")
            sys.exit("getPG: "+str(err))
        return df

    #-------------------------------------------------------------------------#
    def pullPGMeta(self):
        """! @brief Read metadata from a PostgreSQL table and assign to 
        instance property dataframe
        @returns Pandas dataframe
        """
        query = "select * from get_meta_icol('" + self.schema + "', '" + self.table + "');"
        cd.info_log(f"pullPGMeta: Querying DB with: {query}") 
        try:
            dbpg = self.dbut
            with dbpg.getDconx().connect() as cnx:
                df = pd.read_sql_query(query, cnx)
        except Exception as err:
            cd.error_log(f"PullPGMeta: Error executing query: {str(err)}")
            sys.exit("getPG: "+str(err))
        return df

    #-------------------------------------------------------------------------#
    def pullFreqs(self):
        """! @brief Read frequency data for a specific table from a PostgreSQL 
        view and assign to instance property dataframe
        @returns Pandas dataframe of frequencies for columns in specific table
        """
        query = "select * from get_freqs('" + self.schema + "', '" + self.table + "');"
        cd.info_log(f"pullFreqs: Querying DB with: {query}") 
        try:
            dbpg = self.dbut
            with dbpg.getDconx().connect() as cnx:
                df = pd.read_sql_query(query, cnx)
        except Exception as err:
            cd.error_log(f"PullPGMeta: Error executing query: {str(err)}")
            sys.exit("getPG: "+str(err))
        return df

    #-------------------------------------------------------------------------#

    def pullMeta(self):
        """! @brief Read variable, value label and other metadata 
        for a specific table/cardnumber and assigns to dataframe
        @details Reads the following columns of data from view:
        Tab, Name, VarLabel, Public, Value, ValueLabel, Missing
        @returns Pandas dataframe corresponding to table/cardnumber metadata
        """
        query = f"call get_metadata_4_table('{cd.SourceData.table}');"
        cd.info_log(f"pullMeta: Querying DB with: {query}") 
        try:
            dbmt = self.dbut
            with dbmt.getMconx().connect() as cnx:
                df = pd.read_sql_query(query, cnx)
        except Exception as err:
            cd.error_log(f"Error with MySQL connection using {self.parms}")
            sys.exit(f"Error reading MySQL getMeta output for {cd.SourceData.table}"+
                     f"\n\t {str(err)}")
        return df

    #-------------------------------------------------------------------------#

    def killPlot(self, fieldname):
        """! @brief Call SQL procedure to remove an entry 
        for a specific field/variable in the descriptives table
        @details Calls the delete_descriptives_entry procedure.  It was next to 
        impossible to get SQLAlchemy to work, even when copying the examples out of
        their documentation.  This uses the mysql.connector package instead.
        @returns List of results
        """
        query = f"call delete_descriptives_entry('{fieldname}');"
        cd.info_log(f"killPlot: Querying DB with: {query}") 
        try:
            dbmt = self.dbut
            # Note the use of raw_connection here this is different from above
            with dbmt.getMySQLconx() as cnx:
                # eng is an SQLAlchemy engine
                crsr = cnx.cursor()
                myargs = (fieldname,)
                crsr.callproc("delete_descriptives_entry", myargs)
                results = crsr.rowcount
                cnx.commit()
                crsr.close()
                cd.info_log(f"killPlot number of descriptives entries deleted: {results}")
        except myc.Error as err:
            if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
                cd.error_log(f"Error DataPull.killPlot({fieldname}): Wrong user name or password")
            elif err.errno == errorcode.ER_BAD_DB_ERROR:
                cd.error_log(f"Error DataPull.killPlot({fieldname}): Database does not exist")
            else:
                cd.error_log(f"Error DataPull.killPlot({fieldname}) : {str(err)}")
                sys.exit(f"Error MySQL killPlot output for {cd.SourceData.table}"+
                         f"\n\t {str(err)}")
        return str(results)

    #-------------------------------------------------------------------------#
        #-------------------------------------------------------------------------#

    def modifyCardNumPlots(self, crdnum):
        """! @brief Call SQL procedure to modify all entries for variables 
        beloning to a specific CardNumber in the descriptives table
        @details Calls the modify_descriptives_by_cardnumber procedure.  It was next to 
        impossible to get SQLAlchemy to work, even when copying the examples out of
        their documentation.  This uses the mysql.connector package instead.
        @returns List of results
        """
        msg = "<p>No frequency distribution information is available for this variable.</p>"
        proc = "modify_descriptives_by_cardnumber"
        query = f"DataPUll.modifyCardNumPlots : about to call {proc}('{crdnum}','{msg}');"
        cd.info_log(f"modifyCardNumPlots: DB executing: {query}") 
        try:
            dbmt = self.dbut
            cnx = dbmt.getMySQLconx().get_connection() #Get connection from pool
            crsr = cnx.cursor() # Use connection to make a cursor
            myargs = (crdnum, msg)
            crsr.callproc(proc, myargs)
            results = crsr.rowcount
            cnx.commit() # commit results after call
            crsr.close()
            cnx.close()
            cd.info_log(f"modifyCardNumPlots number of descriptives rows modified: {results}")
        except myc.Error as err:
            if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
                cd.error_log(f"Error DataPull.modifyCardNumPlots({crdnum}): Wrong user name or password")
            elif err.errno == errorcode.ER_BAD_DB_ERROR:
                cd.error_log(f"Error DataPull.modifyCardNumPlots({crdnum}): Database does not exist")
            else:
                cd.error_log(f"Error DataPull.modifyCardNumPlots({crdnum}) : {str(err)}")
            sys.exit(f"Error MySQL modifyCardNumPlots output for {cd.SourceData.table}"+
                         f"\n\t {str(err)}")
        return results

 
