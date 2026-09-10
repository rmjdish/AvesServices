#
import sys
import re
import math
import datetime
import numpy as np
import base64
import jinja2
import matplotlib.pyplot as plt
import mpld3
from pathlib import Path, PurePath
from dbutils import DB_Utils
from datapull import DataPull
import commondefs as cd
from ruamel.yaml import YAML

class VarPlots():
    """! @brief Creates histogram or bar charts from metadata and data
    @returns Two files, one CSV with embedded HTML and
    one SQL to load CSV file into metadata database
    """
    # The settings below are default, but are overwridden by the YAML config file
    # Property: _PROGRAM
    #    Class property holding the name of this program
    _PROGRAM = "varplots.py"
    # Property: vpat
    #   Class property RE matching SERNO, NTAG, or nshdid
    vpat = re.compile(r"^([Ss][Ee][Rr][Nn][Oo]|[Nn][Tt][Aa][Gg]\d|[Nn][Ss][Hh][Dd][Ii][Dd]_.*)$")
    # Property: THRESHOLD
    #   Class property for the decision point between crosstabs and histograms
    _THRESHOLD = 5
    # Property: _MINCOUNT
    #   Class property for minimum number of cases for data series to be ploted
    _MINCOUNT = 200
    # Property: _MINCELLCNT
    #   Class property for minimum number of cases for each unique value in data frequencies
    _MINCELLCNT = 50
    # Property: _PROPERTIES_FILE
    #   Class property for the YAML config file holding PostgreSQL info
    _PROPERTIES_FILE = "metaconfig.yaml"
    # Property: _PROPERTIES_YSEC
    #   Class property for name of section with YAML file to be used
    _PROPERTIES_YSEC = "MakeVarPlots"
    _PROPERTIES_DICT = None
    _NUMERIC = ['int64', 'float64', 'double precision', 'smallint',
                'bigint', 'integer']
    _STRING = ['object', 'character', 'character varying', 'text',
               'time without time zone', 'timestamp with time zone', 'date']
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
    # Property: _OUTPUT_PATH
    #    Class property for name of folder to write plots and csv to
    _OUTPUT_PATH = ""
    # Property: _TEMPLATE
    #    Class property for name of overall template for plots and stats
    _TEMPLATE = "./html"
    # Property: _PLOT_TEMPLATE
    #    Class property for name of template used for Matplotlib plots
    _PLOT_TEMPLATE = ""
    # Property: _CAT_TEMPLATE
    #    Class property for name of template used for categorical frequency table
    _CAT_TEMPLATE = ""
    # Property: _CSV_OUTPUT
    #    Class property for name of file holding HTMLised plots 
    _CSV_OUTPUT = ""
    # Property: _SQL_OUTPUT
    #    Class property for name of file holding HTMLised plots 
    _SQL_OUTPUT = ""
    # Property: _FIGSIZE
    #    Class property for name of file holding HTMLised plots 
    _FIGSIZE = (6.0,4.0)
    # Property: _FIG_LAYOUT
    #    Class property for name of file holding HTMLised plots 
    _FIG_LAYOUT = 'tight' #could also be - 'constrained'
    # Property: _FIG_DPI
    #    Class property for name of file holding HTMLised plots 
    _FIG_DPI = 80

    
    


    
    def __init__(self):
        """! @brief Constructor: Create VarPlots instance based 
        on source provided

        @param source - String PG table name or CSV file path
        @param schema - String PG schema name (not used if data in CSV file)
        """
        self.dataframe = None # Dataframe of all columns in the Table
        self.metaframe = None # Dataframe of all metadata from metadata view in robin
        self.icolframe = None # Dataframe of all metadata from icol for Table
        self.freqframe = None # Dataframe of frequency counts for nominal vars in table
        self.properties = VarPlots._PROPERTIES_FILE
        self.dpullobj  = None  # A DataPull instance (assigned later) 
        self.table = None
        self.source = cd.get_source() # Still composite at this point
        self.schema = cd.get_schema() # Possibly None
        self.filename = None
        # Get properties from yaml file
        r = self.readProps()
        if r is None:
            raise EnvironmentError
        else:
            self.props = r # returns a dict
            VarPlots._PROPERTIES_DICT = r
            cd.set_props(r) # make avaialble to other modules
            # Crude logging to web server error log
            print("Logging to file:", self.props["LogPath"], file=sys.stderr)
        cd.set_log(self.props["LogPath"])
        logfile = self.props["WhoAmI"]
        cd.info_log(f"Constructor: Start of {logfile} at {datetime.datetime.now().strftime('%I:%M%p on %B %d, %Y')}")
        # Are we looking for PG table or CSV file
        try:
            tokens = self.source.split(":",1)
        except AttributeError as e:
            cd.error_log(f"Constructor: No source provided: {e}")
            sys.exit(VarPlots._PROGRAM+": No source to read data from:"+e)
        self.out_csv  = tokens[1] + '.csv'
        self.out_html = tokens[1] + '.html'
        self.out_load = tokens[1] + '.sql'
        self.out_mkdn = tokens[1] + '.md'
        if tokens[0].lower() == "table":
            self.table = tokens[1]
        elif tokens[0].lower() == "csv":
            self.filename = tokens[1]
        else:
            self.filename = None
            self.table    = None        

        if (self.table is not None):
            cd.SourceData = cd.DataSourceType(True, self.table, self.schema, None)
            self.dpullobj  = DataPull() # Now we can init DataPull instance
            cd.info_log(f"Set SourceData Table: {cd.SourceData.table} Schema: {cd.SourceData.schema}")
        elif (self.filename is not None):
            cd.SourceData = cd.DataSourceType(False, None, None, self.filename)
            cd.set_filename(self.filename)
            cd.info_log(f"Set SourceData File: {cd.SourceData.file}")
        else:
            raise EnvironmentError
            cd.error_log(f"VarPlots: Cannot make sense of input parameters -- {tokens}")
            sys.exit("Stopping here.")
        self.figlist = [] # List of figures, one for each column
        self.dataframe = None
        self.metaframe = None

    #-------------------------------------------------------------------------#

    def getSource(self):
        """! @brief Return the source of the data for descripion 
        as boolean value:  
        @returns Boolean - True if table, False otherwise
        """
        if self.table is not None:
            return True
        else:
            return False
        
    #-------------------------------------------------------------------------#

    def getFigures(self):
        """!@brief Return the list of figures constructed by metaanalysis 
        @returns List of Matplotlib figure objects
        """
        return self.figlist
        
    #-------------------------------------------------------------------------#

    def getCSVFile(self):
        """! @brief Return the name of the CSV file used to store HTML'ised plots 
        @returns String representation of concatenation PurePaths 
        """
        home = PurePath(VarPlots._OUTPUT_PATH)
        fnam = PurePath(VarPlots._CSV_OUTPUT)
        return str(home / fnam)
        
    #-------------------------------------------------------------------------#

    def getSQLFile(self):
        """! @brief Return the name of the file used to store SQL commands 
        to load descriptives 
        @returns String representation of concatenation PurePaths
        """
        home = PurePath(VarPlots._OUTPUT_PATH)
        fnam = PurePath(VarPlots._SQL_OUTPUT)
        return str(home / fnam)

        
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
    def setParams(self):
        """! @brief Set the values of threshold and other parameters 
        from YAMl properties
        """
        cd.info_log(f"setParams: Reading parameters from YAML file: {VarPlots._PROPERTIES_FILE}")
        thedict = self.props
        if isinstance(thedict,dict):
            VarPlots._PROGRAM = thedict["WhoAmI"]
            VarPlots._THRESHOLD = thedict["THRESHOLD"]
            VarPlots._MINCOUNT = thedict["MINCOUNT"]
            VarPlots._MINCELLCNT = thedict["MINCELLCNT"]
            VarPlots._NUMERIC = thedict["NUMERIC"]
            VarPlots._STRING = thedict["STRING"]
            VarPlots._LOWLIM = thedict["LOWLIM"]
            VarPlots._UPRLIM = thedict["UPRLIM"]
            VarPlots._GAPPCT = thedict["GAPPCT"]
            VarPlots._SEARCH_PATH = thedict["SEARCH_PATH"]
            VarPlots._TEMPLATE = thedict["TEMPLATE"]
            VarPlots._CAT_TEMPLATE = thedict["CAT_TEMPLATE"]
            VarPlots._PLOT_TEMPLATE = thedict["PLOT_TEMPLATE"]
            VarPlots._OUTPUT_PATH = thedict["OUTPUT_PATH"]
            VarPlots._CSV_OUTPUT = thedict["CSV_OUTPUT"]
            VarPlots._SQL_OUTPUT = thedict["SQL_OUTPUT"]
            
    #-------------------------------------------------------------------------#

    def readProps(self):
        """! @brief Read system properties from YAML properties file
        selects the 'metadata' section and returns
        this.  
        
        @returns Section of YAML file specified by _PROPERTIES_YSEC 
        or None on failure
        """
        # Read system properties from YAML properties file
        yaml = YAML(typ="safe", pure=True)
        print(f"Config file: {self.properties}")
        try:
            with open(self.properties, "r", encoding="utf-8") as f:
                p = yaml.load(f)
        except:
            sys.exit(f"readProps: Error reading properties file: {self.properties}")
            return None
        # I'm just interested in the relevant section of p
        sec = p[VarPlots._PROPERTIES_YSEC]
        cd.info_log(f"readProps: Successfully read config file")
        return sec

    #-------------------------------------------------------------------------#

    def getProps(self):
        """! @brief Return the current value of the _PROPERTIES_DICT class property
        @returns String value of property
        """
        return VarPlots._PROPERTIES_DICT
    
    #-------------------------------------------------------------------------#
    def metaAnalysis(self):
        """! @brief Iterates through columns in data and calls do1col 
        method if column is not an identifier.  This is the main method in VarPlots.  
        Ignores identifier columns.
        """
        # Get Threshold parameters
        self.setParams()
        ###################################################################################
        # Write a default plot into all members of the CardNumber == cd.SourceData.table
        pullobj = self.dpullobj # Class that has data pulling methods
        crdnum = cd.SourceData.table
        pullobj.modifyCardNumPlots(crdnum)
        ###################  Pull in data/metadata as Pandas DataFrames ###################
        ddf = pullobj.pullData() # Gets data series
        cd.info_log(f"metaAnalysis: PG table data:\n {ddf.head()}\n")
        mdf = pullobj.pullMeta() # Gets MySQL metadata
        cd.info_log(f"metaAnalysis: MY metadata data:\n {mdf.head()}\n")
        mpgdf = pullobj.pullPGMeta() # Gets PG metadata
        cd.info_log(f"metaAnalysis: PG icol data:\n {mpgdf.head()}\n")
        fpgdf = pullobj.pullFreqs()
        cd.info_log(f"metaAnalysis: PG freqs data:\n {fpgdf.head()}\n")
        self.dataframe = ddf # Dataframe of all columns in the Table to be plotted
        self.metaframe = mdf # Dataframe of all metadata from metadata view in robin
        self.icolframe = mpgdf # Dataframe of all metadata from icol for Table
        self.freqframe = fpgdf # Dataframe of all frequency counts for nominal vars
        ###################################################################################
        cd.info_log(f"metaAnalysis: DataFrame shape: {ddf.shape}, MetaFrame shape: {mdf.shape}, IcolFrame shape: {mpgdf.shape} Freqs shape: {fpgdf.shape}")
        # Iterate over the icolframe 
        for row in self.icolframe.itertuples():
            print(f"Schema: {row.schemaref}, Table: {row.tableref}, Fieldname: {row.fieldname}")
            print(f"\tNumrows: {row.numrows}, NumDiscVals: {row.ndiscvals}, Metric: {row.class_metric}, Isnominal: {row.isnominal}")
            if not VarPlots.vpat.match(row.fieldname):
                # Check this column is NOT an identifier
                if str(self.dataframe[row.fieldname].dtypes) in VarPlots._STRING:
                    # Don't do this one
                    cd.error_log(f"metaAnalysis: *** Non numeric field: {row.fieldname}")
                    continue
                else:
                    self.do1col(row)
        numfigs = len(self.figlist)
        cd.info_log(f"metaAnalysis: Generated {numfigs} charts")

    
    #-------------------------------------------------------------------------#
    def meta2dict(self,uniqs,freqs,colname):
        """! @brief Merge a dataframe of value labels + existing unique values into a dictionary
        Dictionary has form {<numeric value>: <string label>,...}
        @returns: A sorted dictionary
        """
        thedict = dict({}) # Start with empty dictionary, then get meta for colname
        # Slice dataframe using case insensitive match against column name
        thevdf = self.metaframe[self.metaframe["Name"].str.lower() == colname.lower()]
        cd.info_log(f"meta2dict: metadata for {colname} has dims: {thevdf.shape}")
        thevdf = thevdf.loc[:, ["Value","ValueLabel","Missing"]]
        # create the basic dictionary from uniqs
        for idx,v in np.ndenumerate(uniqs):
            thedict.update({v : str(v)})
        # Now replace any that have corresponding labels
        if thevdf.empty:
            # if no metadata then return as is
            return cd.sortdict(thedict)
        else:
            cd.info_log(f"meta2dict: Column: {colname} MetaFrame:\n{thevdf}")
            # Remember that there may be null values in sliced thevdf
            for idx, row in thevdf.iterrows():
                if row['Value'] is not None:
                    val = row['Value'].strip() # Remove excess whitespace
                    lab = row['ValueLabel']
                    if lab is None:
                        continue
                    lab = lab.strip()
                    # Truncate Value labels at 24 chars and wrap after 12
                    lab = lab[:12]+ lab[12:].replace(' ','\n',1)
                    # add newlines after 10
                    lab = lab[:24] # Truncate the labels
                    # check it appears in the distribution (i.e. uniqs)
                    try:
                        val = float(val) # Need it as numeric
                    except ValueError as err:
                        cd.error_log(f"meta2dict: Could not convert {val} to float")
                        continue
                    if np.any(uniqs == val): # Does val occur in uniqs?
                        thedict.update({val: lab})
            cd.info_log (f"meta2dict: Variable {colname} has {uniqs.size} unique values.")
            cd.info_log(f"meta2dict: Variable {colname} has {thedict} value labels.")
            return cd.sortdict(thedict)
    
    #-------------------------------------------------------------------------#
    def metavarlabel(self,colname):
        """! @brief Return the variable label of a column
        @returns String taken from metaframe
        """
        cd.info_log(f"metavarlabel: Getting Variable Label For {colname}")
        # Lower case the column
        thevdf = self.metaframe[self.metaframe["Name"].str.contains(colname, case=False)]
        if thevdf.size == 0:
            # Give up
            cd.info_log("metavarlabel: variable name match failed to return var label.")
            return colname # Can't find label, the name will have to do
        # We have a match
        thevdf = thevdf[["Name","VarLabel","Public"]] # Drop all other columns
        thevdf.reset_index(drop=True,inplace=True) # Reindex from 0
        result = thevdf.loc[0,"VarLabel"] # Return first cell in VarLabel column
        status = thevdf.loc[0,"Public"] # Return the first cell in Public column
        cd.info_log(f"metavarlabel: Column {colname}  - ({result}) has Public = {status}")
        return result
    
    #-------------------------------------------------------------------------#
    def sumStats(self,dseries):
        """! @brief Takes a Numpy data series and computes summary statistics for the series
        Returns the result as an HTML string. Used for histogram data

        @returns A string of HTML comprising a table command
        """
        # Better check data series has some data.
        if dseries.size > 0:
            decarry = np.quantile(dseries,[x for x in np.arange(0.1,1.0,0.1)])
            dmax = np.amax(dseries)
            dmin = np.amin(dseries)
            dmean = np.mean(dseries)
            dstd = np.std(dseries)
            dvar = np.var(dseries)
            dN = dseries.size
        else:
            decarry = np.array({}) # No deciles on empty series
            dmax = 0
            dmin = 0
            dmean = 0
            dstd = 0
            dvar = 0
            dN = 0
        # need to conver to list for template use
        deciles = decarry.tolist()
        _theVars = { 'deciles': deciles,
                     'max' : dmax,
                     'min' : dmin,
                     'mean' : dmean,
                     'std' : dstd,
                     'var' : dvar,
                     'N'   : dN
                     }
        tLoader = jinja2.FileSystemLoader( searchpath=VarPlots._SEARCH_PATH )
        tEnv = jinja2.Environment( loader=tLoader )
        template = tEnv.get_template( VarPlots._TEMPLATE )
        doc = template.render( _theVars )        
        return doc
    
    #-------------------------------------------------------------------------#
    def sumCatStats(self,dseries):
        """! @brief Takes a Numpy data series and computes summary a frequency table for the series
        Returns the result as an HTML string

        @returns A string of HTML comprising a table command
        """
        # Better check data series has some data.
        if dseries.size > 0:
            # nump unique returns ndarrays for uniqs and freqs
            uniqs,freqs  = np.unique(dseries, return_counts=True)
            ftable = dict(zip(uniqs, freqs))
            ftable = cd.sortdict(ftable) # Sort the dictionary
            dmean = np.mean(dseries)
            dstd = np.std(dseries)
            dvar = np.var(dseries)
            dN = dseries.size
            
        else:
            ftable = dict({})
            dmean = 0
            dstd = 0
            dvar = 0
            dN = 0
        # need to conver to list for template use
        _theVars = { 'ftable': ftable,
                     'mean' : dmean,
                     'std' : dstd,
                     'var' : dvar,
                     'N'   : dN
                     }
        home = PurePath(VarPlots._SEARCH_PATH)
        ftem = PurePath(VarPlots._CAT_TEMPLATE)
        tLoader = jinja2.FileSystemLoader( searchpath=str(home) )
        tEnv = jinja2.Environment( loader=tLoader )
        cd.info_log(f"Path to template is {home / ftem}")
        template = tEnv.get_template( str(ftem) )
        doc = template.render( _theVars )        
        return doc
    

    #-------------------------------------------------------------------------#
    def footnoteHBar(self):
        """! @brief Return a string to be used as the footnote for horizontal bar plots
    
        @returns: A string for bar plots
        """
        fnote = "Missing values have been removed {0}".format(VarPlots._BARMOD)
        return fnote
    #-------------------------------------------------------------------------#
    def footnoteHist(self):
        """! @brief Return a string to be used as the footnote for histogram plots

        @returns A string for histograms
        """
        fnote = "Missing values and distribution outside {0} and {1} percentiles removed".format(VarPlots._LOWLIM,VarPlots._UPRLIM)
        return fnote
    #-------------------------------------------------------------------------#
    def do1col(self, row):
        """! @brief Plots a single table column
        @details Takes a column name corresponding to a Pandas DataFrame 
        and does what is required to create a plot of this columns data. 
        Its helper methods encode the resulting plot as a base64 string element 
        and puts adds it to a list of plots called self.figlist
        
        @param row - Named tuple (with elements accessible as row.columnname) 
        from icol corresponding to a column from self.dataframe
        """
        # Assumption self.dataframe and self.metaframe have been populated!
        cd.info_log(f"=============== {row.fieldname} ==================")
        if str(self.dataframe[row.fieldname].dtypes) in VarPlots._NUMERIC:
            # Here's where we make the decision of which plot to do
            if not row.isnominal:
                ######################## HISTOGRAM PLOT ########################################
                # Histogram Plot - Exclude top and bottom 5% of distribution
                self.histoPlot(row)
            else:
                ####################### BAR CHART PLOT ########################################
                # Horizontal Bar Chart Plot
                self.barPlot(row)
        else:
            cd.info_log(f"do1col: *** Problem: Column: {row.fieldname} Type: {str(self.dataframe[row.fieldname].dtypes)} ")
    
    #-------------------------------------------------------------------------#

    def excludeMissing(self, row):
        """! @brief For a specific field generate a numpy data series excluding
        any missing values for this row
        @param row - Named tuple of icol data for column to be plotted
        @returns Numpy array, possibly empty
        """
        NUMS = ['float64', 'int64']
        tpe = self.dataframe[row.fieldname].dtypes
        if tpe in NUMS:
            series = np.array(self.dataframe[row.fieldname]) # Array from column
            series = series[~np.isnan(series)] # Remove NaNs from series
            series = series[series > -4] # Remove all -4...-| values from series
            uniqs, freqs = np.unique(series, return_counts=True)
            cd.info_log(f"excludeMissing: Var {row.fieldname} has initial {uniqs.size} unique values")
            cd.info_log(f"excludeMissing: Initial size of series: {series.size}")
            # Get the metadata info for this column
            mask = [x.casefold() == row.fieldname.casefold() for x in self.metaframe['Name']]
            meta = self.metaframe[mask] # Exclude everything from different columns
            # Are there any missing values for this column?
            if meta.empty:
                cd.info_log(f"excludeMissing: No missing value information found")
                return series
            else:
                # Work to do to exclude missings
                cd.info_log(f"excludeMissing: found following missing value info:\n{meta}")
                # Now get just missing values
                coldata = meta['Missing'].dropna() # a Pandas Series - remove NaNs
                for i, el in enumerate(coldata.to_numpy()): # Throw out any strings left  
                    if isinstance(el, str):
                        coldata.drop(coldata.index[i], inplace=True)
                # Only numexcludeMissingbers left all the NaNs and Strings gone
                mask = [int(x) != 0 for x in coldata]
                # How to apply mask to meta dataframe
                missdf = meta[mask]
                cd.info_log(f"excludeMissing: after mask resulting missing value info:\n{missdf}")
                if missdf.empty:
                    cd.info_log(f"excludeMissing: No missing values left to exlcude")
                    return series
                # Use to cut series. Note some values are integer some are floats so...
                mask = [int(x) if x.isdigit() else float(x) for x in missdf['Value']]
                delmask = [x in mask for x in series]
                series = np.delete(series, delmask)
                cd.info_log(f"excludeMissing: Final size of series: {series.size}")
                return series
        else:
            return np.array()
            
    #-------------------------------------------------------------------------#

    def histoPlot(self, row):
        """! @brief Create histo plot for one column
        @param row - Named tuple of icol data for column to be plotted
        """
        cd.info_log(f"histoPlot: Plotting Histogram")
        # Exclude top and bottom of distribution
        data_series = self.toSeries(row)
        cd.info_log(f"histoPlot: Variable: {row.fieldname} Size: {data_series.size} ")
        uniqs, freqs  = np.unique(data_series, return_counts=True)
        # Make a decision o whether to plot
        if freqs.size == 0:
            cd.info_log(f"histoPlot: No data left to plot for {row.fieldname}")
        cd.info_log(f"histoPlot: Variable  {row.fieldname} has {uniqs.size} unique values")
        nvals  = data_series.size
        cd.info_log(f"histoPlot: Variable: {row.fieldname} Unique Values: {row.ndiscvals}")
        cd.info_log(f"histoPlot: Number of Observations: {nvals}")
        # Set up the plotting vars
        fig, axs = plt.subplots(figsize=VarPlots._FIGSIZE,
                                dpi=VarPlots._FIG_DPI,
                                layout=VarPlots._FIG_LAYOUT
        )
        plt.rc('axes', titlesize=8) #fontsize of the title
        plt.rc('axes', labelsize=8) #fontsize of the x and y labels
        plt.rc('xtick', labelsize=8) #fontsize of the x tick labels
        plt.rc('ytick', labelsize=8) #fontsize of the y tick labels
        data_series = self.filter(data_series)
        stats_html = self.sumStats(data_series)
        # We can set the number of bins with the *bins*
        # keyword argument.
        axs.hist(data_series, bins='auto', edgecolor="black")
        axs.set_xlabel(self.metavarlabel(row.fieldname))
        axs.set_ylabel('Frequency')
        axs.set_title(row.fieldname)
        ############################ Save Files ############################################
        # Finally create png file and encode within HTML templates
        self.toPNGfile(fig,row.fieldname) # Need to save png file first
        fightml = self.toHTML(fig,self.footnoteHist(),stats_html,row.fieldname)  # Then encode as base64 string
        plt.close()
        self.figlist.append({'name' : row.fieldname, 'descriptives' : fightml})
        
    #-------------------------------------------------------------------------#

    def barPlot(self, row):
        """! @brief Create bar chart plot for one column
        @param row - Named tuple of icol data for column to be plotted
        """
        cd.info_log("barplot: Plotting Horizontal Bar Chart")
        # Are there any un-tagged missing values?
        data_series = self.barcheck(row)
        stats_html = self.sumCatStats(data_series)
        # better recompute uniqs and freqs in case dropped outliers
        uniqs,freqs  = np.unique(data_series, return_counts=True)
        # Have to make a decision about whether to press on and plot
        if freqs.size == 0:
            cd.info_log(f"barplot: No data left to plot for {row.fieldname}")
            return
        cd.info_log(f"barplot: uniqs: {uniqs} freqs: {freqs}")
        # Need to incorporate metadata into uniqs by sutstitution
        xlabels = self.meta2dict(uniqs,freqs,row.fieldname)
        xticks  = [x for x in xlabels.keys()]
        xticks.sort()
        #print("X Labels: ",xlabels)
        # Set up the plotting vars
        fig, axs = plt.subplots(figsize=VarPlots._FIGSIZE,
                                dpi=VarPlots._FIG_DPI,
                                layout=VarPlots._FIG_LAYOUT
        )
        plt.rc('axes', titlesize=8) #fontsize of the title
        plt.rc('axes', labelsize=8) #fontsize of the x and y labels
        plt.rc('xtick', labelsize=8) #fontsize of the x tick labels
        plt.rc('ytick', labelsize=8) #fontsize of the y tick labels
        axs.barh(uniqs,freqs,align='center',
                 height=0.5,
                 edgecolor="black")
        axs.set_title(self.metavarlabel(row.fieldname))
        # axs.set_xlabel('Frequencies')
        axs.set_yticks(xticks)
        axs.set_yticklabels(xlabels.values(),rotation=0)
        ############################ Save Files ############################################
        # Finally create png file and encode within HTML templates
        self.toPNGfile(fig,row.fieldname) # Need to save png file first
        fightml = self.toHTML(fig,self.footnoteHBar(),stats_html,row.fieldname)  # Then encode as base64 string
        plt.close()
        self.figlist.append({'name' : row.fieldname, 'descriptives' : fightml})

        
    #-------------------------------------------------------------------------#
    def toHTML(self,thisfig,footnote,thisstats,colname):
        """! @brief Takes a Matplotlib figure object, HTML summary statistics and a column name 
        and uses the helper function to read the corresponding '.png' file matching
        the column name and encode it as a base64 string.  It decorates the resulting 
        string with HTML niceties, embeds it and the summary statistics in a table
        and returns the combined string
        

        @param thisfig - a Matplotlib figure object
        @param footnote - a string caption for the fig
        @param thisstats - a string comprising an HTML table 
        @param colname - string name of data column

        @returns String HTML table command
        """
        # Helper function
        def get_base64_encoded_image(image_path):
            with open(image_path, "rb") as img_file:
                return base64.b64encode(img_file.read()).decode('utf-8')
        # End of helper def'n
        prefix = '<img src="data:image/png;base64,'
        suffix = '" alt="Histogram/Bar Chart Plot" />'
        pth = PurePath(VarPlots._OUTPUT_PATH)
        fnm = PurePath(colname+".png")
        thishtml = get_base64_encoded_image(pth / fnm)
        _theVars = { 'plot': prefix+thishtml+suffix,
                     'footnote' : footnote,
                     'sumstats' : thisstats,
        }
        tLoader = jinja2.FileSystemLoader( searchpath=VarPlots._SEARCH_PATH )
        tEnv = jinja2.Environment( loader=tLoader )
        template = tEnv.get_template( VarPlots._PLOT_TEMPLATE )
        doc = template.render( _theVars )        
        return doc
    
    #-------------------------------------------------------------------------#
    def toPNGfile(self,thisfig,colname):
        """! @brief Takes a Matplotfig figure object and the corresponding column name
        and creates a '.png' file in the current working directory from
        the figure
        @param thisfig - a Matplotlib figure object
        @param colname - string name of data column
        """
        # Contsruct the path using Purepath operators
        pth = PurePath(VarPlots._OUTPUT_PATH)
        fnam = PurePath(colname+".png")
        thisfig.savefig(str(pth / fnam))

    #-------------------------------------------------------------------------#
    def toSeries(self, row):
        """! @brief To return a numpy data series corresponding to the column of the Pandas
        DataFrame self.dataframe with any missing values excluded 
        by setting to Numpy nan and then dropping all nan's

        @param row - Named tuple from icol about single column in table

        @returns Numpy array object
        """
        data_series = self.excludeMissing(row)
        cd.info_log(f"toSeries: Original size of {row.fieldname} column series: {data_series.size}")
        if data_series.size >= 1:
            return data_series
        else:
            cd.info_log(f"toSeries: Data series for {row.fieldname} has 0 size.")
            return np.array([]) # Empty series
    
    #-------------------------------------------------------------------------#
    def filter(self,dseries):
        """! @brief Truncates a Numpy data series above and below percentile limits.
        @param dseries - Numpy array

        @returns A new numpy array 
        """
        data_series = dseries
        #data_series = data_series[data_series >= 0.0] # Remove negs
        if data_series.size > 1:
            low_lim = np.percentile(data_series,VarPlots._LOWLIM)
            upr_lim = np.percentile(data_series,VarPlots._UPRLIM)
            data_series = data_series[data_series >= low_lim]
            data_series = data_series[data_series <= upr_lim]
            return data_series
        else:
            return np.array([])
    #-------------------------------------------------------------------------#
    def barcheck(self,row):
        """! @brief Checks a Numpy data series to be used for bar charts. Excludes
        low cell counts, large gaps and removes outliers.

        @param row - Named tuple from icol row for this column
        @returns A new numpy array 
        """
        data_series = self.excludeMissing(row)
        uniqs,freqs = np.unique(data_series, return_counts=True)
        orig_uniqs = uniqs
        cd.info_log(f"barcheck: After excluding missing values- uniqs: {uniqs} freqs: {freqs}") 
        mask = uniqs[freqs <  VarPlots._MINCELLCNT]
        # Chuck anything out matching the mask
        data_series = data_series[~np.in1d(data_series, mask)]
        # Repeat (To be sure, To be sure!)
        uniqs,freqs = np.unique(data_series, return_counts=True)
        if uniqs.size != orig_uniqs.size:
            VarPlots._BARMOD = "and low cell counts excluded"
        cd.info_log(f"barcheck: After excluding low cell counts- uniqs: {uniqs} freqs: {freqs}")
        """ Remove this gap stuff not sure it's right
        gaps = []
        last = 0 # remember that group theory!
        atstart = True # Need way other than checking last == 0 (0 might occur)
        # compute the gaps between ticks
        if uniqs.size < 1:
            # This shouldn't be the case
            cd.error_log(f"barcheck: *** Problem uniqs size: {uniqs.size} freqs size: {freqs.size}")
            return data_series
        for ele in np.nditer(uniqs): # Numpy way of iterating over elements
            if atstart:
                last = ele
                atstart = False
            else:
                gaps += [ele - last]
                last = ele
        cd.info_log(f"barcheck: Gaps between uniqs {gaps}")
        if len(gaps) < 2: # Less than 2 unique values don't do anything
            return data_series
        # Total space for ticks
        space = uniqs[-1] - uniqs[0] + 1
        # is percentage of last gap too big?
        if (gaps[-1] / space * 100) > VarPlots._GAPPCT:
            # Remove the last set of freqs
            delvalue = uniqs[-1]
            data_series = data_series[data_series != delvalue]
            VarPlots._BARMOD = "and upper outliers excluded"
        else:
            VarPlots._BARMOD = ""
            # uniqs and freq will now changes so beware!
        """
        return data_series
    #-------------------------------------------------------------------------#
    def removenegs(self,dseries):
        """! @brief Takes a Numpy data series and removes all negative values

        @param dseries - Numpy array

        @returns A new numpy array 
        """
        data_series = dseries
        data_series = data_series[data_series >= 0.0]
        return data_series
    #-------------------------------------------------------------------------#
