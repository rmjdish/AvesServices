import csv
import sys
import os
#os.environ['NUMEXPR_MAX_THREADS'] = '16'
#os.environ['NUMEXPR_NUM_THREADS'] = '12'
from pathlib import PurePosixPath, PureWindowsPath
import re
import psycopg2
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import mpld3
from ruamel.yaml import YAML
import logging
import collections
import base64
import jinja2

DataSourceType = collections.namedtuple("DataSourceType","isrdb table schema filename")
MyVersion = "Descriptives-0.8 "



"""
class: DB_Utils
    Encapsulate details of opening databases
"""
class DB_Utils():
    """
    Method: __init__
        Initialises instance attributes and checks properties 
    
    Parameters:
        propdict - dictionary object derived from YAML file
        source - string argument with csv: or table: prefix
    """
    def __init__(self, propdict, source=None):
        # Properties: 
        #   datacnxn - instance property: connection object for data
        #   datacrsr - instance property: cursor from existing data connection
        #   metacnxn - instance property: connection objcet for metadata
        #   source   - instance property: data from file or table encoded here
        self.metacnxn = None
        self.datacnxn = None
        self.datacrsr = None
        self.source   = source
        if isinstance(propdict,dict):
           self.parms = propdict
        else:
            sys.exit("DB_Utils: No valid dictionary of properties")
        if Descriptives.SourceData.isrdb:
            metaok = self.metaDB()
            dataok = self.dataDB()
        else:
            print("Failure to connect to source data and/or metadata.")
        
    #-------------------------------------------------------------------------#
    """
    Method: getDconx
        Return the data connection instance variable value
    """
    def getDconx(self):
        return self.datacnxn

    #-------------------------------------------------------------------------#
    """
    Method: getMconx
        Return the metadata connection instance variable value
    """

    def getMconx(self):
        return self.metacnxn

    #-------------------------------------------------------------------------#
    """
    Method: getDcrsr
        Return the data connection cursor instance variable value
    """

    def getDcrsr(self):
        return self.datacrsr

    
    #-------------------------------------------------------------------------#

    """ 
    Method: metaDB
        Makes use of the mysql.connector package to create db connections
        to MySQL databases.
        As as side effect assigns connections to instance property metaDB

    Returns:
        True on success or False on failure
    """
    def metaDB(self):
        import mysql.connector
        from mysql.connector import errorcode
        # create MySQL connection with parameters
        try:
            conn = mysql.connector.connect(user=self.parms["MyUID"],
                                    password=self.parms["MyPwd"],
                                    host=self.parms["MyServer"],
                                    database=self.parms["MyDBase"])
        except mysql.connector.Error as err:
            if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
                print(MyVersion+"MySQL Error: Bad user name or password",file=sys.stderr)
            elif err.errno == errorcode.ER_BAD_DB_ERROR:
                print(MyVersion+"MySQL Error: Database does not exist:",self.parms["MyDBase"],
                  file=sys.stderr)
            else:
                print(MyVersion+"MySQL Error: ", err, file=sys.stderr)
            return False
        self.metacnxn = conn
        return True

    #-------------------------------------------------------------------------#

    """ 
    Method: dataDB
        Makes use of the psycopg2 package to create db connections
        to PostgreSQL databases.
        As as side effect assigns connections to instance property dataDB

    Returns:
        True on success or False on failure
    """
    def dataDB(self):
        # create PostgreSQL connection with parameters
        try:
            conn = psycopg2.connect(user=self.parms["PgUID"],
                                    password=self.parms["PgPwd"],
                                    host=self.parms["PgServer"],
                                    database=self.parms["PgDBase"])
        except psycopg2.Error as err:
            print(MyVersion+"Psycopg2 Error: ",err.pgerror,file=sys.stderr)
            print(MyVersion+"Error with DB connection to: ",self.parms["PgDBase"],
                  file=sys.stderr)
            print(MyVersion+"Connection Parameter Server: ",self.parms["PgServer"],
                  file=sys.stderr)
            return False
        self.datacnxn = conn
        try:
            alccxn = conn.cursor()
        except psycopg2.Error as err:
            print(MyVersion+"Error attempting to create cursor to: %s",self.parms["PgDBase"],
                  file=sys.stderr)
            print(MyVersion+'Main error: ',err.pgerror,
                  "gives diagnostic",err.diag.message_primary,
                  "on cursor", err.cursor,
                  file=sys.stderr)
            return False
        self.datacrsr = alccxn
        return True

#=============================================================================#
"""
class: DataPull
    Gets data from data and metadata databases and returns it as Pandas dataframes
"""
class DataPull():

    """
    Method: __init__
        Initialises instance objects with parameters derived from YAML file

    Parameters:
        propdict - Dictionary of properties derived from YAML file
    """
    def __init__(self, propdict):
        self.source = source
        if isinstance(propdict,dict):
           self.parms = propdict
        else:
            sys.exit(MyVersion+"DataPull: No valid dictionary of properties")
        

    #-------------------------------------------------------------------------#
    """
    Method: pullData
        Read in data from correct source and assign to instance property dataframe

    Returns:
        Pandas DataFrame

    See Also:
        <getPG> <getCSV>
    """
    def pullData(self):
        print("SourceData: ",Descriptives.SourceData)
        if Descriptives.SourceData.isrdb:
            return self.getPG(Descriptives.SourceData.schema,
                           Descriptives.SourceData.table)
        else:
            return self.getCSV(Descriptives.SourceData.filename)

    #-------------------------------------------------------------------------#
    """
    Method: getCSV
        Read in data from a CSV file and assign to instance property dataframe
    """
    def getCSV(self,filename):
        if filename is None:
            df = pd.DataFrame() #Empty DataFrame
        else:
            try:
                df = pd.read_csv(filename)
            except:
                sys.exit(MyVersion+'Error reading CSV file: ' + filename)
        return df
    
    #-------------------------------------------------------------------------#
    """
    Method: getPG
        Read data from a PostgreSQL table and assign to instance property dataframe
    """
    def getPG(self, schname, tabname):
        query = "select * from " + schname + "." + tabname + ";"
        try:
            dbpg = DB_Utils(self.parms)
            df = pd.read_sql_query(query,dbpg.datacnxn)
            dbpg.getDconx().close()
        except Exception as err:
            sys.exit(MyVersion+"getPG: "+str(err))
        return df

    #-------------------------------------------------------------------------#

    """
    Method: pullMeta
        Read variable label and value label information and assign to dataframe
    """
    def pullMeta(self):
        query = "call getMeta('"+Descriptives.SourceData.table+"');"
        try:
            dbmt = DB_Utils(self.parms)
            df = pd.read_sql_query(query,dbmt.metacnxn)
            dbmt.getMconx().close()
        except Exception as err:
            sys.exit(MyVersion+'Error reading MySQL getMeta output for ('+
                     Descriptives.SourceData.table+') -\n '+str(err))
        return df
    

#=============================================================================#
"""
Class PlotColumn:
    Plots an individual column as a Pandas/Matplotlib plot and returns as
    a Matplotlib figure
"""
class PlotColumn():
    def __init__(self,dframe):
        self.dataframe = dframe
    
    #-------------------------------------------------------------------------#
    
    def doXTab(self,field):
        # Does this dataframe have a sex column?
        if 'sex' in self.dataframe.columns:
            xtab = pd.crosstab(index=self.dataframe[field],
                               columns=self.dataframe["sex"],
                               dropna=False,
                               margins=True)
        else:
            xtab = pd.crosstab(index=self.dataframe[field],
                               columns="count",
                               dropna=False,
                               margins=True)
        thisfig = xtab.to_html()    
        return thisfig
    
    #-------------------------------------------------------------------------#
    def doHist(self,field):
        fig, axs  = plt.subplots(figsize=(7,5), dpi=100)
        # Plot Histogram on x
        x = self.dataframe[field]
        plt.hist(x)
        plt.gca().set(title='Frequency Histogram of ' + field, ylabel='Frequency');
        #plt.show()
        return thisfig

#=============================================================================#
"""
Class WritePlot:
    Takes a Matplotlib figure and writes it to a HTML file in embedded image
    format. Also creates MySQL source file for batch loading, if required.
"""
class WritePlot():
    """
    method: __init__
        Intialises instance attributes for figures and files

    Parameters:
        figs - Matplotlib list of figures containing plots
    """
    def __init__(self,figs):
        self.figlist = figs
        self.csvfile = None
        self.sqlfile = None
        
    #-------------------------------------------------------------------------#

    def getXTabs(self):
        for (columnName, columnData) in self.dataframe.iteritems():
            if not Descriptives.vpat.match(columnName):
                fightml = self.doXTab(columnName)
                self.figlist.append({'name' : columnName, 'descriptives' : fightml})
            else:
                print(MyVersion+"Found identifier:",columnName)
        print(MyVersion+"Generated",len(self.figlist),"Pandas Crosstabs")
        
    #-------------------------------------------------------------------------#
    """
    Method: putCSV
        Writes CSV file from instance dict attribute figlist
   """
    def putCSV(self, outcsv):
        self.csvfile = outcsv
        try:
            with open(outcsv, 'w') as csvfile:
                writer = csv.DictWriter(csvfile,
                                        fieldnames=['name','descriptives'],
                                        lineterminator='$$',
                                        dialect='excel',
                                        quoting=csv.QUOTE_NONNUMERIC,
                                        quotechar='|')
                writer.writeheader()
                writer.writerows(self.figlist)
        except IOError:
            sys.exit(MyVersion+"I/O error; stopping")
    
    #-------------------------------------------------------------------------#
    Method: putLoadSQL
       Writes an SQL file with MySQL LOAD Data instructions for CSV file data
       using CSV file file path

    Parameters:
        outsql - String full path name for SQL file name
    """
    def putLoadSQL(self,outsql):
        self.sqlfile = outsql
        prefix = os.getcwd().split(os.path.sep)
        prefix.append(self.csvfile)
        fullpath = "/".join(prefix)
        try:
            with open(outsql, 'w') as lfile:
                lfile.write("-- 'Loads CSV file produced by Descriptives.py into robin.descriptives'\n")
                lfile.write('LOAD DATA LOCAL INFILE "'+fullpath+'"\n')
                lfile.write("REPLACE INTO TABLE `descriptives`\n")
                lfile.write("CHARACTER SET 'utf8'\n")
                lfile.write("FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '|'\n")
                lfile.write("LINES TERMINATED  BY '$$'\n")
                lfile.write("IGNORE 1 LINES\n")
                lfile.write("(name,descriptives)\n")
        except IOError:
                sys.exit(MyVersion+"I/O error; stopping")


#=============================================================================#


"""
class: Descriptives
    Creates histogram or bar charts from metadata and data 

Returns:
    Two files, one CSV with embedded HTML and one SQL to load CSV file into metadata database
"""
class Descriptives():
    # Property: _PROGRAM
    #    Class property holding the name of this program
    _PROGRAM = "MakeDescriptives.py"
    
    # Property: vpat
    #   Class property RE matching SERNO, NTAG, or nshdid
    vpat = re.compile(r"^([Ss][Ee][Rr][Nn][Oo]|[Nn][Tt][Aa][Gg]\d|[Nn][Ss][Hh][Dd][Ii][Dd]_.*)$")
    
    # Property: NARGS
    #   Class property for number of required arguments to program
    NARGS = 2
    # Property: SourceData
    #   Named tuple to hold input parameter data
    SourceData = DataSourceType(False,None,None,None)

    # Property: THRESHOLD
    #   Class property for the decision point between crosstabs and histograms
    _THRESHOLD = 5
    # Property: _MINCOUNT
    #   Class property for minimum number of cases for data series to be ploted
    _MINCOUNT  = 200
    # Property: _MINCELLCNT
    #   Class property for minimum number of cases for each unique value in data frequencies
    _MINCELLCNT = 50
    # Property: _PROPERTIES_FILE
    #   Class property for the YAML config file holding PostgreSQL info

    _PROPERTIES_FILE = "./MakeDescriptives.yaml"
    # Property: _PROPERTIES_YSEC
    #   Class property for name of section with YAML file to be used
    _PROPERTIES_YSEC = "MakeDescriptives"
    _PROPERTIES_DICT = None
    _NUMERIC = ['int64','float64','double precision','smallint','bigint','integer']
    _STRING = ['object','character','character varying','text','time without time zone',
               'timestamp with time zone','date']
    # Property: _LOWLIM
    #    Class property for lower percentile cut off for data series 
    _LOWLIM = 5  # Percentile lower cut-off
    # Property: _UPRLIM
    #    Class property for upper percentile cut off for data series 
    _UPRLIM = 95 # Percentile upper cut-off
    # Property: _GAPPCT
    #    Class property for percentage of gap before cut off of upper value in bar charts
    _GAPPCT = 20 # Bar chart upper gap percentage
    _BARMOD = ""
    # Property: _SEARCH_PATH
    #    Class property for name of folder to contain Jinja2 templates
    _SEARCH_PATH = ""
    # Property: _TEMPLATE
    #    Class property for name of overall template for plots and stats
    _TEMPLATE = ""
    # Property: _PLOT_TEMPLATE
    #    Class property for name of template used for Matplotlib plots
    _PLOT_TEMPLATE = ""
    # Property: _CAT_TEMPLATE
    #    Class property for name of template used for categorical frequency table
    _CAT_TEMPLATE = ""

    """
    Function: sortdict:
        A utility function (not a method) to sort a dictiornary by its keys
    Parameters:
        adict - Dictionary object
    Returns:
        A copy of adict sorted on key values
    """
    def sortdict(adict):
        newdict = dict({})
        k = adict.keys()
        for idx in sorted(k):
            newdict.update({idx: adict[idx]})
        return newdict

    
    """
    Constructor: __init__
        Create Descriptives instance based on source provided

    Parameters:
        source - String PG table name or CSV file path
        schema - String PG schema name (not used if data in CSV file)
    """
    def __init__(self,source,schema):
        self.properties = Descriptives._PROPERTIES_FILE
        self.table = None
        self.schema = None
        self.filename = None
        # Get properties from yaml file
        r = self.readProps()
        if r == False:
            raise EnvironmentError
        else:
            self.props = r
            Descriptives._PROPERTIES_DICT = r
            # Crude logging to web server error log
            print(MyVersion+"Logging to file:",self.props["LogPath"],file=sys.stderr)
        logging.basicConfig(filename=self.props["LogPath"],level=logging.DEBUG,format='%(asctime)s %(message)s')
        logging.info(MyVersion+"Constructor: Start of %s",self.props["WhoAmI"])
        # Are we looking for PG table or CSV file
        try:
            tokens = source.split(":",1)
        except AttributeError as e:
            logging.error(MyVersion+"Constructor: No source provided: %s",e)
            sys.exit(Descriptives._PROGRAM+": No source to read data from:"+e)
        self.out_csv  = tokens[1] + '.csv'
        self.out_html = tokens[1] + '.html'
        self.out_load = tokens[1] + '.sql'
        self.out_mkdn = tokens[1] + '.md'
        if tokens[0].lower() == "table":
            self.table = tokens[1]
            self.schema = schema
        elif tokens[0].lower() == "csv":
            self.filename = tokens[0]
        else:
            self.filename = None
            self.table    = None        

        if (self.table is not None):
            #DataSourceType = collections.namedtuple("DataSourceType",
            # "isrdb table schema file")
            Descriptives.SourceData = DataSourceType(
                True,self.table,self.schema,None)
        else:
            Descriptives.SourceData = DataSourceType(
                False,None,None,self.filename)
        self.figlist = []
        self.dataframe = None
        self.metaframe = None

    #-------------------------------------------------------------------------#

    """
    Method: getSource
        Return the source of the data for descripion as boolean value:  
        True if table, False otherwise

    Returns:
        Boolean

    """
    def getSource(self):
        if self.table is not None:
            return True
        else:
            return False
        
    #-------------------------------------------------------------------------#

    """
    Method: getFigures
        Return the list of figures constructed by metaanalysis 

    Returns:
        List of Matplotlib figure objects

    """
    def getFigures(self):
        return self.figlist
        
    #-------------------------------------------------------------------------#

    """
    Method: getCSVFile
        Return the name of the CSV file used to store HTML'ised plots 

    """
    def getCSVFile(self):
        return self.out_csv
        
    #-------------------------------------------------------------------------#

    """
    Method: getSQLFile
        Return the name of the file used to store SQL commands to load descriptives 

    Returns:
        String file name

    """
    def getSQLFile(self):
        return self.out_load
        
    #-------------------------------------------------------------------------#

    """
    Method: getHTMLFile
        Return the name of the file used to store raw HTML descriptive plots 

    """
    def getHTMLFile(self):
        return self.out_html
        
    #-------------------------------------------------------------------------#

    """
    Method: getMKDFile
        Return the name of the file used to Markdown for descriptives 

    """
    def getMKDFile(self):
        return self.out_mkdn
        
    #-------------------------------------------------------------------------#
    """
    Method: setParams
        Set the values of threshold and other parameters from YAMl properties
    """
    def setParams(self):
        logging.info(MyVersion+"Reading parameters from YAML file: %s",
                     Descriptives._PROPERTIES_FILE)
        thedict = self.props
        if isinstance(thedict,dict):
            Descriptives._PROGRAM = thedict["WhoAmI"]
            Descriptives._THRESHOLD = thedict["THRESHOLD"]
            Descriptives._MINCOUNT = thedict["MINCOUNT"]
            Descriptives._MINCELLCNT = thedict["MINCELLCNT"]
            Descriptives._NUMERIC = thedict["NUMERIC"]
            Descriptives._STRING = thedict["STRING"]
            Descriptives._LOWLIM = thedict["LOWLIM"]
            Descriptives._UPRLIM = thedict["UPRLIM"]
            Descriptives._GAPPCT = thedict["GAPPCT"]
            Descriptives._SEARCH_PATH = thedict["SEARCH_PATH"]
            Descriptives._TEMPLATE = thedict["TEMPLATE"]
            Descriptives._CAT_TEMPLATE = thedict["CAT_TEMPLATE"]
            Descriptives._PLOT_TEMPLATE = thedict["PLOT_TEMPLATE"]
            
    #-------------------------------------------------------------------------#

    """
    Method: readProps
        Read system properties from YAML properties file
        selects the 'metadata' section and returns
        this.  

    Returns:
        Section of YAML file specified by _PROPERTIES_YSEC or None on failure
    """
    def readProps(self):
        # Read system properties from YAML properties file
        yaml = YAML(typ="safe", pure=True)
        try:
            with open(Descriptives._PROPERTIES_FILE, "r", encoding="utf-8") as f:
                p = yaml.load(f)
        except:
            logging.error(MyVersion+"readProps: Error reading properties file: %s", Descriptives._PROPERTIES_FILE)
            return None
        # I'm just interested in the relevant section of p
        sec = p[Descriptives._PROPERTIES_YSEC]
        return sec

    #-------------------------------------------------------------------------#

    """
    Method: getProps
        Return the current value of the _PROPERTIES_DICT class property
    
    Returns:
        String value of property
    """
    def getProps(self):
        return Descriptives._PROPERTIES_DICT
    
    #-------------------------------------------------------------------------#
    """
    Method: metaAnalysis
        Iterates through columns in data and calls do1col method if column is
        not an identifier.  Ignores identifier columns.
    """
    def metaAnalysis(self):
        # Get Threshold parameters
        self.setParams()
        # Pull in data as Pandas DataFrame
        pullobj = DataPull(self.getProps())
        ddf = pullobj.pullData()
        mdf = pullobj.pullMeta()
        self.dataframe = ddf # Dataframe of all columns in the Table
        self.metaframe = mdf # Dataframe of all metadata for columns
        logging.info(MyVersion+"metaAnalysis: DataFrame shape: %s, MetaFrame shape: %s",ddf.shape,mdf.shape)
        for (columnName, columnData) in ddf.iteritems():
            # Iterates over the DataFrame columns,
            # returning a tuple with the column name and the content as a Series.
            if not Descriptives.vpat.match(columnName):
                # Check this column is NOT an identifier
                if str(ddf[columnName].dtypes) in Descriptives._STRING:
                    # Don't do this one
                    continue
                else:
                    self.do1col(columnName)
        logging.info(MyVersion+"metaAnalysis: Generated %s charts",len(self.figlist))

    
    #-------------------------------------------------------------------------#
    """
    Method: meta2dict
        Merge a dataframe of value labels + existing unique values into a dictionary
        Dictionary has form {<numeric value>: <string label>,...}

    Returns:
        A sorted dictionary
    """
    def meta2dict(self,uniqs,freqs,colname):
        thedict = dict({}) # Start with empty dictionary, then get meta for colname
        # Slice dataframe using case insensitive match against column name
        thevdf = self.metaframe[self.metaframe["Name"].str.lower() == colname.lower()]
        logging.info(MyVersion+"meta2dict: metadata for %s has dims: %s",
                     colname,thevdf.shape)
        thevdf = thevdf.loc[:,["Value","ValueLabel","Missing"]]
        
        thevdf = thevdf[thevdf['Missing'] != 1] # Cut Missing Values
        # create the basic dictionary from uniqs
        for idx,v in np.ndenumerate(uniqs):
            thedict.update({v : str(v)})
        # Now replace any that have corresponding labels
        if thevdf.empty:
            # if no metadata then return as is
            return Descriptives.sortdict(thedict)
        else:
            logging.info(MyVersion+"meta2dict: Column: %s MetaFrame:\n%s",colname,thevdf)
            # Remember that there may be null values in sliced thevdf
            for idx, row in thevdf.iterrows():
                if row['Value'] is not None:
                    val = row['Value'].strip() # Remove excess whitespace
                    lab = row['ValueLabel'].strip()
                    # Truncate Value labesl at 24 chars and wrap after 12
                    lab = lab[:12]+ lab[12:].replace(' ','\n',1)
                    # add newlines after 10
                    lab = lab[:24] # Truncate the labels
                    # check it appears in the distribution (i.e. uniqs)
                    val = float(val) # Need it as numeric
                    if np.any(uniqs == val): # Does val occur in uniqs?
                        thedict.update({val: lab})
            logging.info(MyVersion+"meta2dict: Variable %s has %s unique values.",colname,uniqs.size)
            logging.info(MyVersion+"meta2dict: Variable %s has %s value labels.",colname,thedict)
            return Descriptives.sortdict(thedict)
    
    #-------------------------------------------------------------------------#
    """
    Method: metavarlabel
        Return the variable label of a column

    Returns:
        A string
    """
    def metavarlabel(self,colname):
        logging.info(MyVersion+"metavarlabel: Getting Variable Label For %s",colname)
        # Lower case the column
        thevdf = self.metaframe[self.metaframe["Name"].str.contains(colname, case=False)]
        if thevdf.size == 0:
            # Give up
            logging.info(MyVersion+"metavarlabel: variable name match failed to return var label.")
            return colname # Can't find label, the name will have to do
        # We have a match
        thevdf = thevdf[["Name","VarLabel","Public"]] # Drop all other columns
        thevdf.reset_index(drop=True,inplace=True) # Reindex from 0
        result = thevdf.loc[0,"VarLabel"] # Return first cell in VarLabel column
        status = thevdf.loc[0,"Public"] # Return the first cell in Public column
        logging.info(MyVersion+"metavarlabel: Column %s  - (%s) has Public = %s",colname, result, status)
        return result
    
    #-------------------------------------------------------------------------#
    """
    Method: sumStats
        Takes a Numpy data series and computes summary statistics for the series
        Returns the result as an HTML string. Used for histogram data

    Returns:
        A string of HTML comprising a table command
    """
    def sumStats(self,dseries):
        # Better check data series has some data.
        if dseries.size > 0:
            decarry = np.quantile(dseries,[x for x in np.arange(0.1,1.0,0.1)])
            dmax = np.amax(dseries)
            dmin = np.amin(dseries)
            dmean = np.mean(dseries)
            dstd = np.std(dseries)
            dvar = np.var(dseries)
        else:
            decarry = np.array({}) # No deciles on empty series
            dmax = 0
            dmin = 0
            dmean = 0
            dstd = 0
            dvar = 0
        # need to conver to list for template use
        deciles = decarry.tolist()
        _theVars = { 'deciles': deciles,
                     'max' : dmax,
                     'min' : dmin,
                     'mean' : dmean,
                     'std' : dstd,
                     'var' : dvar
                     }
        tLoader = jinja2.FileSystemLoader( searchpath=Descriptives._SEARCH_PATH )
        tEnv = jinja2.Environment( loader=tLoader )
        template = tEnv.get_template( Descriptives._TEMPLATE )
        doc = template.render( _theVars )        
        return doc
    
    #-------------------------------------------------------------------------#
    """
    Method: sumCatStats
        Takes a Numpy data series and computes summary a frequency table for the series
        Returns the result as an HTML string

    Returns: 
        A string of HTML comprising a table command
    """
    def sumCatStats(self,dseries):
        # Better check data series has some data.
        if dseries.size > 0:
            # nump unique returns ndarrays for uniqs and freqs
            uniqs,freqs  = np.unique(dseries, return_counts=True)
            ftable = dict(zip(uniqs, freqs))
            ftable = Descriptives.sortdict(ftable) # Sort the dictionary
            dmean = np.mean(dseries)
            dstd = np.std(dseries)
            dvar = np.var(dseries)
            
        else:
            ftable = dict({})
            dmean = 0
            dstd = 0
            dvar = 0
        # need to conver to list for template use
        _theVars = { 'ftable': ftable,
                     'mean' : dmean,
                     'std' : dstd,
                     'var' : dvar
                     }
        tLoader = jinja2.FileSystemLoader( searchpath=Descriptives._SEARCH_PATH )
        tEnv = jinja2.Environment( loader=tLoader )
        template = tEnv.get_template( Descriptives._CAT_TEMPLATE )
        doc = template.render( _theVars )        
        return doc
    

    #-------------------------------------------------------------------------#
    """
    Method: footnoteHBar
        Return a string to be used as the footnote for horizontal bar plots

    Returns:
        A string
    """
    def footnoteHBar(self):
        fnote = "All missing values (including negatives) have been removed {0}".format(Descriptives._BARMOD)
        return fnote
    #-------------------------------------------------------------------------#
    """
    Method: footnoteHist
        Return a string to be used as the footnote for histogram plots

    Returns:
        A string
    """
    def footnoteHist(self):
        fnote = "Missing values and distribution outside {0} and {1} percentiles removed".format(Descriptives._LOWLIM,Descriptives._UPRLIM)
        return fnote
    #-------------------------------------------------------------------------#
    """
    Method: do1col
        Takes a column name corresponding to a Pandas DataFrame and does what is
        required to create a plot of this columns data. It encodes the resulting
        plot as a base64 string element and puts adds it to a list of plots
        called self.figlist

    Parameters:
        colname - String data column name from self.dataframe
    """
    def do1col(self,colname):
        # Assumption self.dataframe and self.metaframe have been populated!
        logging.info(MyVersion+"=============== %s ==================",colname)
        if str(self.dataframe[colname].dtypes) in Descriptives._NUMERIC:
            # fig, axs = plt.subplots(figsize=(7, 4))
            # Exclude top and bottom of distribution
            data_series = self.toSeries(colname)
            logging.info(MyVersion+"do1col: Variable: %s Size: %s ",colname,data_series.size)
            # Should we use a bar graph or a histogram?
            uniqs,freqs  = np.unique(data_series, return_counts=True)
            logging.info(MyVersion+"do1col: Variable %s Unique Values:\n %s",colname,uniqs)
            logging.info(MyVersion+"do1col: Frequencies: \n %s",freqs)
            nuvals = uniqs.size
            nvals  = data_series.size
            if nvals >= 1:
                nratio = nuvals / nvals
            else:
                nratio = 0
            logging.info(MyVersion+"do1col: Variable: %s Unique Values: %s",colname,nuvals)
            logging.info(MyVersion+"do1col: Threshold: %s Unique Ratio: %s",Descriptives._THRESHOLD,nratio)
            #print("Unique Values:",uniqs)
            #print("Frequencies:",freqs)
            logging.info(MyVersion+"do1col: Number of Observations: %s",nvals)
            if nvals < Descriptives._MINCOUNT:
                # Too risky to plot small number of observarions
                logging.info(MyVersion+"do1col: Observations too few - not plotting this.")
                return
            else:
                # These data should be plotted, but which type of plot?
                fig, axs = plt.subplots(figsize=(8,6))
                plt.rc('axes', titlesize=8) #fontsize of the title
                plt.rc('axes', labelsize=8) #fontsize of the x and y labels
                plt.rc('xtick', labelsize=8) #fontsize of the x tick labels
                plt.rc('ytick', labelsize=8) #fontsize of the y tick labels
                # Here's where we make the decision of which plot to do
                if nratio >= Descriptives._THRESHOLD:
                    # Histogram Plot - Exclude negative values and top and bottom 5% of distribution
                    data_series = self.filter(data_series)
                    stats_html = self.sumStats(data_series)
                    # Low Cell Counts? Skip and close fig
                    if np.any(np.less(freqs,Descriptives._MINCELLCNT)):
                        logging.info(MyVersion+"do1col: *** Warning Low Cell Count - Hist Plot Suppressed ***")
                        plt.close() # Clean up opened fig
                        return
                    # We can set the number of bins with the *bins*
                    # keyword argument.
                    logging.info(MyVersion+"do1col: Plotting Histogram")
                    axs.hist(data_series, bins='auto', edgecolor="black")
                    axs.set_xlabel(self.metavarlabel(colname))
                    axs.set_ylabel('Frequency')
                    axs.set_title(colname)
                    # Add a footnote below and to the right side of the chart
                    fig.text(.5, .02, self.footnoteHist(),size='x-small', ha='center')
                elif uniqs.size >= 1:  # Doesn't pass threshold for histo but data to plot
                    # Horizontal Bar Chart Plot
                    # Are there any un-tagged missing values?
                    data_series = self.barcheck(data_series)
                    stats_html = self.sumCatStats(data_series)
                    # better recompute uniqs and freqs in case dropped outliers
                    uniqs,freqs  = np.unique(data_series, return_counts=True)
                    # Low Cell Counts? Skip and close fig
                    if np.any(np.less(freqs,Descriptives._MINCELLCNT)):
                        logging.info(MyVersion+"do1col: *** Warning Low Cell Count - Bar Plot Suppressed ***")
                        plt.close() # Clean up opened figures
                        return
                    # Need to incorporate metadata into uniqs by sutstition
                    logging.info(MyVersion+"do1col: Plotting Horizontal Bar Chart")
                    xlabels = self.meta2dict(uniqs,freqs,colname)
                    xticks  = [x for x in xlabels.keys()]
                    xticks.sort()
                    #print("X Labels: ",xlabels)
                    axs.barh(uniqs,freqs,align='center',
                             height=0.5,
                             edgecolor="black")
                    axs.set_title(self.metavarlabel(colname))
                    axs.set_xlabel('Frequencies')
                    axs.set_yticks(xticks)
                    axs.set_yticklabels(xlabels.values(),rotation=0)
                    # Add a footnote below and to the right side of the chart
                    fig.text(.5, .05, self.footnoteHBar(),size='x-small', ha='center')
                else:
                    logging.error(MyVersion+"do1col: Column: %s neither good enough for histogram or bar chart.",colname)
                    plt.close()
                    return
                # Finally create png file and encode within HTML templates
                self.toPNGfile(fig,colname) # Need to save png file first
                fightml = self.toHTML(fig,stats_html,colname)  # Then encode as base64 string
                plt.close()
                self.figlist.append({'name' : colname, 'descriptives' : fightml})
        else:
            logging.info("Column: %s Type: %s ",colname,str(self.dframe[colname].dtypes))
    
    #-------------------------------------------------------------------------#
    """
    Method: toHTML
        Takes a Matplotlib figure object, HTML summary statistics and a column name 
        and uses the helper function to read the corresponding '.png' file matching
        the column name and encode it as a base64 string.  It decorates the resulting 
        string with HTML niceties, embeds it and the summary statistics in a table
        and returns the combined string

    Parameters:
        thisfig - a Matplotlib figure object
        thisstats - a string comprising an HTML table 
        colname - string name of data column

    Returns:
        String HTML table command
    """
    def toHTML(self,thisfig,thisstats,colname):
        # Helper function
        def get_base64_encoded_image(image_path):
            with open(image_path, "rb") as img_file:
                return base64.b64encode(img_file.read()).decode('utf-8')
        prefix = '<img src="data:image/png;base64,'
        suffix = '" alt="Histogram/Bar Chart Plot" />'
        thishtml = get_base64_encoded_image(colname+".png")
        _theVars = { 'plot': prefix+thishtml+suffix,
                     'sumstats' : thisstats,
        }
        tLoader = jinja2.FileSystemLoader( searchpath=Descriptives._SEARCH_PATH )
        tEnv = jinja2.Environment( loader=tLoader )
        template = tEnv.get_template( Descriptives._PLOT_TEMPLATE )
        doc = template.render( _theVars )        
        return doc
    
    #-------------------------------------------------------------------------#
    """
    Method: toPNGfile
        Takes a Matplotfig figure object and the corresponding column name
        and creates a '.png' file in the current working directory from
        the figure
    """
    def toPNGfile(self,thisfig,colname):
        # BIG TO DO: change to save fig as PNG and create MD file?
        thisfig.savefig(colname+".png")

    #-------------------------------------------------------------------------#
    """
    Method: toSeries
        To return a numpy data series corresponding to the column of the Pandas
        DataFrame self.dataframe with any missing values excluded 
        by setting to Numpy nan and then dropping all nan's

    Parameters:
        colname - string data column name

    Returns:
        Numpy array object
    """
    def toSeries(self,colname):
        tp = self.dataframe[colname].dtypes
        # Get Metadata for this column Table column names are lowercase
        metadf = self.metaframe[self.metaframe["Name"] == colname.upper()]
        logging.info(MyVersion+"toSeries: Missing Value DataFrame:\n %s",metadf[["Name","Value","Missing"]])
        metadf = metadf.loc[:,["Value","ValueLabel","Missing"]]
        # Create  dataframe of only those triples where Missing == 1
        missdf = metadf[metadf["Missing"] != 0] # This can be empty
        if not missdf.empty:
            # Return Values where Missing not 0
            missme = np.array(missdf.loc[:,["Value"]])
            # Flatten it
            missme = missme.flatten()
            logging.info(MyVersion+"toSeries: Missing Values to be screened: %s",
                         missme)
        else:
            missme = np.array([]) # Nothing to do
        try:
            missme = missme.astype(float) # Convert to floats
        except:
            missme = np.array([])
        if tp in ['float64', 'int64']:
            data_series = np.array(self.dataframe[colname]) # Array from column
            data_series = self.removenegs(data_series) # Remove all negative values
            logging.info(MyVersion+"toSeries: Original size of %s column series: %s",
                         colname,
                         data_series.size)
            # Now set elements of data_series to nan where equal to element of missme
            for x in missme:
                data_series[data_series == x] = np.nan # Set missing values to np.nan
            data_series = data_series[~np.isnan(data_series)] # Drop np.nans
            logging.info(MyVersion+"toSeries: Final size of %s column series: %s",
                         colname,
                         data_series.size)
            
            # Get list of unique values for this series
            if data_series.size >= 1:
                return data_series
            else:
                logging.info(MyVersion+"toSeries: Data series for %s has 0 size.",colname)
                return np.array([]) # Empty series
        else:
            logging.info(MyVersion+"toSeries: Variable %s has type %s",colname,tp)
    
    #-------------------------------------------------------------------------#
    """
    Method: filter
        Truncates a Numpy data series above and below percentile limits.
        Returns a Numpy data series

    Parameters:
        dseries - Numpy array

    Returns:
        A new numpy array 
    """
    def filter(self,dseries):
        data_series = dseries
        data_series = data_series[data_series >= 0.0] # Remove negs
        if data_series.size > 1:
            low_lim = np.percentile(data_series,Descriptives._LOWLIM)
            upr_lim = np.percentile(data_series,Descriptives._UPRLIM)
            data_series = data_series[data_series >= low_lim]
            data_series = data_series[data_series <= upr_lim]
            return data_series
        else:
            return np.array([])
    #-------------------------------------------------------------------------#
    """
    Method: barcheck
        Checks a Numpy data series to be used for bar charts for large gaps
        and removes outliers.
        Returns a Numpy data series
    Parameters:
        dseries - Numpy array

    Returns:
        A new numpy array 
    """
    def barcheck(self,dseries):
        data_series = dseries
        uniqs,freqs = np.unique(data_series, return_counts=True)
        gaps = []
        last = 0 # remember that group theory!
        atstart = True # Need way other than checking last == 0 (0 migh occur)
        # compute the gaps between ticks
        for ele in np.nditer(uniqs): # Numpy way of iterating over elements
            if atstart:
                last = ele
                atstart = False
            else:
                gaps += [ele - last]
                last = ele
        logging.info(MyVersion+"barcheck: Gaps between uniqs %s",gaps)
        if len(gaps) < 2: # Less than 2 unique values don't do anything
            return data_series
        # Total space for ticks
        space = uniqs[-1] - uniqs[0] + 1
        # is percentage of last gap too big?
        if (gaps[-1] / space * 100) > Descriptives._GAPPCT:
            # Remove the last set of freqs
            delvalue = uniqs[-1]
            data_series = data_series[data_series != delvalue]
            Descriptives._BARMOD = "and upper outliers excluded"
        else:
            Descriptives._BARMOD = ""
            # uniqs and freq will now changes so beware!
        return data_series
    #-------------------------------------------------------------------------#
    """
    Method: removenegs
        Takes a Numpy data series and removes all negative values
        Returns a Numpy data series
    Parameters:
        dseries - Numpy array

    Returns:
        A new numpy array 
    """
    def removenegs(self,dseries):
        data_series = dseries
        data_series = data_series[data_series >= 0.0]
        return data_series


#-------------------------------------------------------------------------#


if __name__ == "__main__":
    if (len(sys.argv) - 1) != Descriptives.NARGS:
        print("Usage:")
        print(Descriptives._PROGRAM," <CardNumber> <Schema> ")
        print("<CardNumber> ::= [table:|csv:]CardNumber")
        print("<Schema> ::= Schema Containing CardNumber Table | None if CSV")
        sys.exit("Wrong number of arguments; need "+str(Descriptives.NARGS)+" got "+str(len(sys.argv)-1))
    else:
        # Pass on the crucial arguments
        source = sys.argv[1]
        schema = sys.argv[2]
    descrips = Descriptives(source,schema)
    descrips.metaAnalysis() # Go through all the columns
    savplts = WritePlot(descrips.getFigures()) # Create all the .png files
    savplts.putCSV(descrips.getCSVFile()) # Read, encode .pngs and bundle to CSV file
    savplts.putLoadSQL(descrips.getSQLFile()) # Create SQL file to load CSV into DB
