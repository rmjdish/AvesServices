# MetaPlot -- compute and install showcase like plots 
import sys,os
from pathlib import Path, PurePath
os.environ['NUMEXPR_MAX_THREADS'] = '16'
os.environ['NUMEXPR_NUM_THREADS'] = '12'
#from pathlib import PurePosixPath, PureWindowsPath
import commondefs as cd
# Add cwd folder to search path
sys.path.append(Path.cwd())
# import other modules of this program
from varplots import VarPlots
from writeplot import WritePlot

class MetaPlot:
    """! Top level class to run variable plots
    """
    _NARGS = 2
    _PROGRAM = "metaplot.py "
    
    def main():
        """! @brief Checks arguments and calls VarPlots to pruduce plots
        if args are ok
        """
        if (len(sys.argv) - 1) != MetaPlot._NARGS:
            print("Usage:")
            print(MetaPlot._PROGRAM," <CardNumber> <Schema> ")
            print("<CardNumber> ::= [table:|csv:]CardNumber")
            print("<Schema> ::= Schema Containing CardNumber Table | None if CSV")
            sys.exit("Wrong number of arguments; need "+str(MetaPlot._NARGS)+" got "+str(len(sys.argv)-1))
        else:
            # Pass on the crucial arguments
            source = sys.argv[1]
            cd.set_source(source) # Source is a composite at this point
            schema = sys.argv[2]
            cd.set_schema(schema) # Either schema name or None
            descrips = VarPlots()
            descrips.metaAnalysis() # Go through all the columns
            # Create all the .png files
            savplts = WritePlot(descrips.getFigures())
            # Read, encode .pngs and bundle to CSV file
            savplts.putCSV(descrips.getCSVFile())
            # Create SQL file to load CSV into DB
            savplts.putLoadSQL(descrips.getSQLFile())
      
if __name__ == "__main__":
    MetaPlot.main()
