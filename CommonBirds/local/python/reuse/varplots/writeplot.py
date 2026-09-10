import os
import matplotlib.pyplot as plt
import mpld3
import csv
from pathlib import Path, PurePath
import commondefs as cd

class WritePlot():
    """! @brief Takes a Matplotlib figure and writes it 
    to a HTML file in embedded image format. Also creates 
    MySQL source file for batch loading, if required.
    """
    def __init__(self,figs):
        """! @brief Constructor - Intialises instance attributes 
        for figures and files
        
        @param figs - Matplotlib list of figures containing plots
        """
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
                cd.info_log(f"Found identifier: {columnName}")
        cd.info_log(f"Generated {len(self.figlist)} Pandas Crosstabs")
        
    #-------------------------------------------------------------------------#

    def putCSV(self, outcsv):
        """! @brief Writes CSV file from instance dict 
        attribute figlist
        @param outcsv - string path t output file for plot data
        """
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
            sys.exit(+"I/O error; stopping")
    
    #-------------------------------------------------------------------------#

    def putLoadSQL(self,outsql):
        """! @brief Writes an SQL file with MySQL LOAD Data instructions
        for CSV file data using CSV file file path
        
        @param outsql - String full path name for SQL file name
        """
        self.sqlfile = outsql
        try:
            with open(outsql, 'w') as lfile:
                lfile.write("-- 'Loads CSV file produced by Descriptives.py into robin.descriptives'\n")
                lfile.write('LOAD DATA LOCAL INFILE "'+self.csvfile+'"\n')
                lfile.write("REPLACE INTO TABLE `descriptives`\n")
                lfile.write("CHARACTER SET 'utf8'\n")
                lfile.write("FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '|'\n")
                lfile.write("LINES TERMINATED  BY '$$'\n")
                lfile.write("IGNORE 1 LINES\n")
                lfile.write("(name,descriptives)\n")
        except IOError:
                sys.exit("I/O error; stopping")


