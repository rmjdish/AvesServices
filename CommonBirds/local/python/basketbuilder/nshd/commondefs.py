"""! @brief This is a holding pen for def'ns common across the app
"""
__all__ = ['CommonDefs', 'DBaseError', 'MetaOpenError',
           'MetaAccessError', 'DataOpenError', 'DataAccessError']
import sys
import time
import collections
import logging
import datetime
from ruamel.yaml import YAML



class CommonDefs:
    """! @brief Common properties and methods to be used by all nshd classes
    """

    # Class attributes/variables
    _TASK = None
    _METADBASE = None # MySQL Meta DB
    _DATADBASE = None # PG Data DB
    _SCRMDBASE = None # Scrambling DB
    _SCHEMA = None
    _PROPERTIES = None # A dict of parameter settings
    _PROPFILE = "/opt/swift/bobthebuilder.yaml" # the default path to a YAML config file
    _PROPSECT = "bobthebuilder" # the default section of the YAML file
    _LOGFILE = None
    _LOGNAME = "/xnat/san/SST/baskets/BasketBuilder.log"
    _LOGGER = None
    _THETIME = time.strftime("%Y-%m-%d_%H:%M:%S") #create a time variable which will be used in all the filenames.
    _DATA_DICT = None
    _VALUE_LABELS = None
    _BASKET_NAME = None
    _USER_NAME = None
    _DATA_DFRAME = None
    _TROLLEY_DFRAME = None
    _RAW_FOLDER = None
    _RAW_FIlE = None
    _SCRAM_FOLDER = None
    _SCRAM_FILE = None
    _IDENTIFIER = 'serno'
    _NTAG_DATAFRAME = None
    _SEQ_DATAFRAME = None

    # Class access functions
    @classmethod
    def sortdict(cls, adict):
        """! @brief A class level utility function (not a method) to sort
        a dictiornary by its keys
        @param adict - Dictionary object
        @returns A copy of adict sorted on key values or an empty dict
        """
        newdict = dict({})
        if not isinstance(adict, dict):
            return newdict
        k = adict.keys()
        for idx in sorted(k):
            newdict.update({idx: adict[idx]})
        return newdict

    @classmethod
    def ThisVersion(cls):
        if "scramblebasket" in sys.argv[0].lower():
            return "Scramble-4.0"
        elif "buildbasket" in sys.argv[0].lower():
            return "BasketBuilder-4.0"
        else:
            return "NSHD_Module-4.0"

    @classmethod
    def get_trolley_dframe(cls):
        return cls._TROLLEY_FILES

    @classmethod
    def set_trolley_dframe(cls, tframe):
        cls._TROLLEY_DFRAME = tframe

    @classmethod
    def get_task(cls):
        return cls._TASK

    @classmethod
    def set_task(cls, task):
        cls._TASK = task

    @classmethod
    def get_identifier(cls):
        return cls._IDENTIFIER

    @classmethod
    def set_identifier(cls, id):
        cls._IDENTIFIER = id

    @classmethod
    def get_bskname(cls):
        return cls._BASKET_NAME

    @classmethod
    def set_bskname(cls, bskid):
        cls._BASKET_NAME = bskid

    @classmethod
    def get_usrname(cls):
        return cls._USER_NAME

    @classmethod
    def set_usrname(cls, usrid):
        cls._USER_NAME = usrid

    @classmethod
    def get_data_dict(cls):
        return cls._DATA_DICT

    @classmethod
    def set_data_dict(cls, dframe):
        cls._DATA_DICT = dframe

    @classmethod
    def get_data(cls):
        return cls._DATA_DFRAME

    @classmethod
    def set_data(cls, dframe):
        cls._DATA_DFRAME = dframe

    @classmethod
    def get_value_labels(cls):
        """! @brief Deliver the Value Lables metadata for a basket.
        @returns Pandas DataFrame
        """
        return cls._VALUE_LABELS

    @classmethod
    def set_value_labels(cls, dframe):
        cls._VALUE_LABELS = dframe

    @classmethod
    def get_ntags(cls):
        return cls._NTAG_DATAFRAME

    @classmethod
    def set_ntags(cls, dframe):
        cls._NTAG_DATAFRAME = dframe

    @classmethod
    def get_seq(cls):
        return cls._SEQ_DATAFRAME

    @classmethod
    def set_seq(cls, dframe):
        cls._SEQ_DATAFRAME = dframe

    @classmethod
    def get_thetime(cls):
        return cls._THETIME

    @classmethod
    def set_metadbase(cls, db):
        cls._METADBASE = db

    @classmethod
    def get_metadbase(cls):
        return cls._METADBASE

    @classmethod
    def set_scrmdbase(cls, db):
        cls._SCRMDBASE = db

    @classmethod
    def get_scrmdbase(cls):
        return cls._SCRMDBASE

    @classmethod
    def set_datadbase(cls, db):
        cls._DATADBASE = db

    @classmethod
    def get_datadbase(cls):
        return cls._DATADBASE

    @classmethod
    def set_schema(cls, sch):
        cls._SCHEMA = sch

    @classmethod
    def get_schema(cls):
        return cls._SCHEMA

    @classmethod
    def set_propfile(cls, fname):
        cls._PROPFILE = fname

    @classmethod
    def get_propfile(cls):
        return cls._PROPFILE

    @classmethod
    def set_props(cls, adict):
        if isinstance(adict, dict):
            cls._PROPERTIES = adict

    @classmethod
    def get_props(cls):
        return cls._PROPERTIES

    @classmethod
    def get_propsect(cls):
        return cls._PROPSECT

    @classmethod
    def set_raw_folder(cls, folder):
        if not folder is None:
            cls._RAW_FOLDER = folder

    @classmethod
    def get_raw_folder(cls):
        return cls._RAW_FOLDER

    @classmethod
    def set_raw_file(cls, afile):
        if not afile is None:
            cls._RAW_FILE = afile

    @classmethod
    def get_raw_file(cls):
        return cls._RAW_FILE

    @classmethod
    def set_scram_folder(cls, folder):
        if not folder is None:
            cls._SCRAM_FOLDER = folder

    @classmethod
    def get_scram_folder(cls):
        return cls._SCRAM_FOLDER

    @classmethod
    def set_scram_file(cls, folder):
        if not folder is None:
            cls._SCRAM_FILE = folder

    @classmethod
    def get_scram_file(cls):
        return cls._SCRAM_FILE

    @classmethod
    def set_log(cls, afile=cls._LOGNAME):
        if not afile is None:
            cls._LOGFILE = afile
        else:
            cls._LOGFILE = cls._LOGNAME
        print(f"Logging to {cls._LOGFILE}")
        cls._LOGGER = logging.getLogger(cls._LOGFILE) # Get an instance
        cls._LOGGER.setLevel(logging.DEBUG)# Set lowest level to log
        cls._LOGGER.addHandler(logging.FileHandler(filename=afile)) # Attach a handler

    @classmethod
    def debug_log(cls, msg):
        if not cls._LOGFILE is None:
            cls._LOGGER.debug(f"DEBUG: {get_thetime()} {msg}")

    @classmethod
    def info_log(cls, msg):
        if not cls._LOGFILE is None:
            cls._LOGGER.info(f"INFO:   {get_thetime()} {msg}")

    @classmethod
    def error_log(cls, msg):
        if not cls._LOGFILE is None:
            cls._LOGGER.error(f"ERROR: {get_thetime()} {msg}")


    #------------------------ Instance Methods ------------------------------#
            
    def __init__(self):
        CommonDefs.set_log() # Set name for log file
        props = self.readprops()
        if not isinstance(props, dict):
            CommonDefs.info_log(f"CommonDefs: failed to read properties: {props}")
            sys.exit(f"CommonDefs: failed to read properties: {props}")
        CommonDefs.set_props(props)
        # Capture arguments
        self.parse_args()
        # Assign some default values
        CommonDefs.set_datadbase(props["pyPgDBase"])
        CommonDefs.set_metadbase(props["pyMyDBase"])
        CommonDefs.set_scrmdbase(props["pyMyDBaseScram"])
         

    #-------------------------------------------------------------------------#
    def parse_args(self):
                # Check correct number of argusments provided when executing the script
        # This version expects arg1 = basketID, arg2 = Swift username
        if len(sys.argv) != 3:
            CommonDefs.error_log(f"{CommonDefs.ThisVersion()} CommonDefs:parse_args: Arguments should be basket ID and username")
            quit()
        else:
            CommonDefs.set_bskname(sys.argv[1])
            CommonDefs.info_log(f"{CommonDefs.ThisVersion()} CommonDefs:parse_args: basket ID is {CommonDefs.get_bskname()}")
            CommonDefs.set_usrname(sys.argv[2])
            CommonDefs.info_log(f"{CommonDefs.ThisVersion()} CommonDefs:parse_args: username is {CommonDefs.get_usrname()}")
            


    #-------------------------------------------------------------------------#

    def readprops(self):
        """! @brief Read system properties from YAML properties file
        selects the '' section and returns
        this.  
        
        @returns Section of YAML file specified by _PROPSECT 
        or None on failure
        """
        # Read system properties from YAML properties file
        yaml = YAML(typ="safe", pure=True)
        try:
            with open(CommonDefs.get_propfile(), "r", encoding="utf-8") as f:
                p = yaml.load(f)
        except:
            sys.exit(f"{CommonDefs.ThisVersion()} readprops: Error reading properties file: {CommonDefs.get_propfile()}")
            return None
        # I'm just interested in the relevant section of p
        sec = p[get_propsect()]
        CommonDefs.info_log(f"{CommonDefs.ThisVersion()} readProps: Successfully read config file {CommonDefs.get_propfile()}")
        if isinstance(sec,list):
            sec = sec.pop()
        return sec

 
class DBaseError(Exception):
    """! @brief Basic exception class for database related errors """
    
    def __init__(self, message, dbname):
        super().__init__(message)
        self.timestamp = datetime.datetime.now()
        self.dbname = dbname

class MetaOpenError(DBaseError):
    """! @brief Error opening/accessing metadata database"""
    def __init__(self, message):
        super().__init__(message, get_metadbase())

class MetaAccessError(DBaseError):
    """! @brief Error opening/accessing metadata database"""
    def __init__(self, message):
        super().__init__(message, get_metadbase())

class DataOpenError(DBaseError):
    """! @brief Error opening/accessing data database"""
    def __init__(self, message):
        super().__init__(message, get_datadbase())

class DataAccessError(DBaseError):
    """! @brief Error opening/accessing data database"""
    def __init__(self, message):
        super().__init__(message, get_datadbase())

    
