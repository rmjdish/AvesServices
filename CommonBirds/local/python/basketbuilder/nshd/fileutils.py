import os
import fnmatch as fn
import re
import pandas as pd
from pathlib import Path
import shutil as sh
# import package components
from . commondefs import *

class FileOps():


    def __init__(self):
        super().__init__()  # This now initializes the shared state
        self.props = self.get_props()
        self.bskname = self.get_bskname() # Get the basket name from CommonDefs class
        # Instance property that holds the path to the folder where files will be created
        self.path = self.props["pyLinPath"]
        self.debug_log(f"{self.ThisVersion()} FileOps:__init__ BskNm:{self.bskname}")
        self.debug_log(f"{self.ThisVersion()} FileOps:__init__ Path:{self.path}")
        self.buildfolder = self.path+os.sep+self.bskname+os.sep+self.props["pyBuildSuf"]
        self.debug_log(f"{self.ThisVersion()} FileOps:__init__ buildfolder:{self.buildfolder}")
        self.scramfolder = self.path+os.sep+self.bskname+os.sep+self.props["pyScramSuf"]
        self.debug_log(f"{self.ThisVersion()} FileOps:__init__ scramfolder:{self.scramfolder}")
                


    def setupFolder(self):
        """! @brief Makes sure the folder exists where CSV or other files will be created 
            as a result of running the build process.

            Now respects the suffix 'raw' or 'scrambled' to work with Jay.
            The file path is now constructed as:
                pyLinPath+os.sep+basketID+os.sep+'raw'+os.sep+
            @returns True on success and False on failure
        """   
        try:
            self.debug_log(f"{self.ThisVersion()} setupFolder: buildfolder is {self.buildfolder}")
            self.debug_log(f"{self.ThisVersion()} setupFolder: scramfolder is {self.scramfolder}")
            if not os.path.exists(self.buildfolder):  # Create folder called basketID if it already does not exist.
                self.info_log(f"{self.ThisVersion()} Creating directories: {self.buildfolder} and {self.scramfolder}")
                os.makedirs(self.buildfolder)
                os.makedirs(self.scramfolder)
                set_raw_folder(self.buildfolder)
                self.debug_log(f"{self.ThisVersion()} setupFolder: buildfolder (commondefs) {get_raw_folder()}") 
                return True
            else:
                self.info_log(f"{self.ThisVersion()} Directory already exists: {self.buildfolder}")
                set_raw_folder(self.buildfolder)
                self.debug_log(f"{self.ThisVersion()} setupFolder: buildfolder (commondefs) {get_raw_folder()}") 
                return True
        except:
            self.error_log(f"{self.ThisVersion()} Can't create files/folders in path: {self.buildfolder}")
            return False
        
    ###############################################################################

    def scrambleFolder(self):
        """! @brief Checks CSV data file already exists
        @returns Boolean True if data CSV folder is there there and populated, False otherwise
        """
        self.debug_log(f"{self.ThisVersion()} FileOps:scrambleFolder: scramblefolder is {self.scramfolder}") 
        if not os.path.exists(self.buildfolder):
            # if there's no data to scramble stop here
            self.error_log(f"{self.ThisVersion()} FileOps:scrambleFolder: Can't find input folder: {self.buildfolder}")
            return False
        self.set_raw_folder(self.buildfolder) # remember location
        if not os.path.exists(self.scramfolder):
            # doesn't exist so have to create it but must be empty
            try:
                self.info_log(f"{self.ThisVersion()} FileOps:scrambleFolder: Creating scrambling folder: {self.scramfolder}")
                os.makedirs(self.scramfolder)
            except (IOError, OSError) as err:
                self.error_log(f"{self.ThisVersion()} FileOps:scrambleFolder: Error trying to create folder: {self.scramfolder} ==> {err}")
                return False
        else:
            # There might be previously scrambled stuff here so get rid of it
            here = Path(self.scramfolder) # get a handle on the existing folder
            for child in here.iterdir():
                self.info_log(f"{self.ThisVersion()} scrambleFolder:  Removing existing scrabled file: {child}")
                child.unlink(missing_ok=True) # remove file or symbolic link
        # Look for evidence of existing built dataset
        self.set_scram_folder(self.scramfolder) # remember location
        self.info_log(f"{self.ThisVersion()} scrambleFolder: Looking for files to scramble in {self.buildfolder}")
        # Test if there are CSV files and copy the value_labels*.csv and variable_labels*.csv to scramfolder
        datflist = [f for f in os.listdir(self.buildfolder) if fn.fnmatch(f, self.get_bskname()+'*.csv')]
        csvflist = [f for f in os.listdir(self.buildfolder) if not fn.fnmatch(f, self.get_bskname()+'*')]
        if len(datflist) > 0:
            self.info_log(f"{self.ThisVersion()} scrambleFolder: Looks like there is/are {len(datflist)} input CSV file(s) in {self.buildfolder}")
            # remember file names need to be turned into full path names to do the copying
            # These two re's need to be consistent with altered names
            for f in csvflist: # Copy metadata files
                try:
                    self.info_log(f"{self.ThisVersion()} Copying {f} to {self.scramfolder+os.sep}")
                    sh.copyfile(self.buildfolder+os.sep+f,self.scramfolder+os.sep+f)
                except (IOError, OSError) as err:
                    self.error_log(f"{self.ThisVersion()} scrambleFolder: Error copying {f} : {err}")
                    return False
            for f in datflist:
                self.info_log(f"{self.ThisVersion()} scrambleFolder: Looking at {f} as data file in directory: {self.buildfolder}")
                if fn.fnmatch(f,self.get_bskname()+'*.csv'):
                    # This CSV file matches the pattern and therefore is a potential file to be scrambled
                    outfile    = self.get_bskname()+'_SCRAMBLED.csv'
                    self.set_scram_file(self.scramfolder+os.sep+outfile)
                    self.set_raw_file(self.buildfolder+os.sep+f)
                    self.info_log(f"{self.ThisVersion()} FileOps:scrambleFolder:  Setting Raw File to csv file: {self.get_raw_file()}")                    
                    self.info_log(f"{self.ThisVersion()} FileOps:scrambleFolder:  Setting Scrambled File to csv file: {self.get_scram_file()}")
                    return True
            return False
        else:
            self.error_log(f"{self.ThisVersion()} FileOps:scrambleFolder Error, no matching files in {datflist}")
            return False
                      

    ###############################################################################

    def read_dataset(self,theinput):
        """! @brief Read CSV data file to be scrambled and store in CommonDefs
        @param theinput - String full path to CSV file
        """
        self.info_log(f"{self.ThisVersion()} FileOps:read_dataset: attempting to get data from {theinput}")
        try:
            dfData = pd.read_csv(theinput, sep=",", header='infer', on_bad_lines='skip', index_col=False, dtype='unicode')
        except:
            self.error_log(f"{self.ThisVersion()} FileOps:read_dataset: Error reading CSV file into Pandas DataFrame.")
            quit()
        dfData.columns = dfData.columns.str.lower() # convert all column headers to lower case
        # Now save the result into CommonDefs
        self.set_data(dfData)


    ###############################################################################

    def dataExists(self):
        """! @brief Takes self.path and tests if a built CSV file exists at the end
            of the path with the basket name as the prefix.
            @returns True if such a file exists, False otherwise
        """
        self.debug_log(f"{self.ThisVersion()} dataExists: looking for existing data in {self.get_raw_folder()}")
        candidates = [f for f in os.listdir(self.get_raw_folder()) if fn.fnmatch(f,self.get_bskname()+'*.csv')]
        if (len(candidates) < 1):
            self.error_log(f"{self.ThisVersion()} FileOps:dataExists: Basket file does not exist yet.")
            return False
        else:
            self.set_raw_file(candidates[0])
            self.info_log(f"{self.ThisVersion()} FileOps: dataExists: One or more basket files exist : {candidates}")
            self.read_dataset(Path(self.get_raw_folder()) / self.get_raw_file()) # Read CSV file and store as DataFrame    
            return True

    ###############################################################################

    
    def writeSPSS(self):
        """! @brief Creates a text file of SPSS syntax that will read a CSV file corresponding to the basket of variables
              specified by self.bskname
        """
        # File: SPSS Script File
        #   Creating the SPSS Script file using the object properties *self.path*, *self.bskname*, and *self.thetime* 
        #    with extension ".sps"
        self.debug_log(f"{self.ThisVersion()} FileOps:writeSPSS: Data Dictionary dims {get_data_dict().shape}")
        self.debug_log(f"{self.ThisVersion()} FileOps:writeSPSS: Value Labels dims {get_value_labels().shape}")
        sfile = self.get_raw_folder()+os.sep+'SPSS_Script_'+self.get_bskname()+'_'+str(self.get_thetime())+'.sps'
        try:
            f = open(sfile,'w+')
        except IOError as err:
            self.error_log(f"{self.ThisVersion()} FileOps:writeSPSS: Error opening file {sfile} : {err}")
            quit()
        self.debug_log(f"{self.ThisVersion()} FileOps:writeSPSS: Opened file {sfile}")    
        f.write('*************SPSS SCRIPT START************* \n \n \n \n')
        f.write('Note: Use SPSS Windows menu to open the CSV dataset. \n \n')
        f.write('*****SPSS Variable labels***** \n \n')
        for row in get_data_dict().itertuples():
            if row[1] is not None and \
               row[2] is not None and \
               not fn.fnmatch(row[1],'serno'):
                f.write('VARIABLE LABELS' + ' ' + row[1]+ ' ' +"'"+row[2]+"'"+"."+'\n')
        f.write('\n \n \n')
        f.write('*****SPSS Value labels***** \n \n')
        for row in self.get_value_labels().itertuples():
            f.write('ADD VALUE LABELS' + ' ' + row[1] + ' ' +row[2]+' '+"'"+row[3]+"'"+"."+'\n')
        f.write('\n \n \n')
        f.write('*************SPSS SCRIPT END************* \n')
        f.close()

    ##################################################################################################################################
    def writeStata(self):
        """! @brief Creates a text file of Stata syntax that will read a CSV file corresponding to the basket of variables
                specified by self.bskname
        """
        # File: Stata Script File
        #    Creating the STATA Script file using the object properties *self.path*, *self.bskname*, and *self.thetime* 
        #    with the extension ".do"
        self.debug_log(f"{self.ThisVersion()} FileOps:writeStata: Data Dictionary dims {self.get_data_dict().shape}")
        self.debug_log(f"{self.ThisVersion()} FileOps:writeStata: Value Labels dims {self.get_value_labels().shape}")
        sfile = self.get_raw_folder()+os.sep+'STATA_Script_'+self.get_bskname()+'_'+str(self.get_thetime())+'.do'
        try:
            f = open(sfile,'w+')
        except IOError as err:
            self.error_log(f"{self.ThisVersion()} writeStata: Error opening {sfile} : {err}")
            quit()
        self.debug_log(f"{self.ThisVersion()} writeStata: Opened file {sfile}")
        f.write('*************STATA SCRIPT START************* \n \n \n \n')
        f.write('***Location of files*** \n')
        f.write('cd "Please specify the directory filepath for the files" \n \n')
        f.write('***Import dataset*** \n')
        f.write('import delim using <specify filename>, delim(",") varnames(1) encoding("utf-8") clear \n \n')
        f.write('***STATA Variable labels***** \n \n')
        for row in get_data_dict().itertuples():
            if row[1] is not None and \
               row[2] is not None and \
               not fn.fnmatch(row[1], 'serno'):
                f.write('label variable' + ' ' + str.lower(row[1])+ ' ' +'"'+row[2]+'"''\n')
        f.write('\n \n \n')
        f.write('***STATA value labels***** \n \n')
        f.write('**Defining value labels** \n \n')
        for row in self.get_value_labels().itertuples():
            f.write('label define'+' '+str.lower(row[1])+' '+row[2]+' '+'"'+row[3]+'"' +', modify''\n')
        f.write('\n \n \n')
        f.write('**Applying defined labels** \n \n')
        valist = self.get_value_labels()['name'].drop_duplicates().values.tolist()
        for item in valist:
            f.write('label values' + ' ' + str.lower(item) + ' ' + str.lower(item)+'\n')
        f.write('\n \n \n')
        f.write('*************STATA SCRIPT END************* \n')
        f.close()
        #End of STATA Script

    def getscoop(self):
        """! Looks for a list of files and scoops them up into a zip file.
        """
        import shutil
        db = DButils()
        db.getTrolleyMetaData()
        pathtofolder = self.get_outputfolder()+os.sep
        for row  in  self.get_trolley_dframe().itertuples():
            try:
                shutil.copy2(getattr(row,'location'),pathtofolder)
            except:
                self.error_log(f"{self.ThisVersion()} FileOps:getscoop: Failed to copy file from {location} to {pathtofolder}, but not quitting.")

            
    #################################################################################################################################################################################
        

