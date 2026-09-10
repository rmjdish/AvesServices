"""! @brief This is a holding pen for def'ns common across the app
"""
import sys
import collections
import logging
from ruamel.yaml import YAML

def sortdict(adict):
    """! @brief A class level utility function (not a method) to sort
    a dictiornary by its keys
    @param adict - Dictionary object
    @returns A copy of adict sorted on key values or an empty dict
    """
    newdict = dict({})
    if not isinstance(adict,dict):
        return newdict
    k = adict.keys()
    for idx in sorted(k):
        newdict.update({idx: adict[idx]})
    return newdict

# Class variables
__DBASE = None # 
__SCHEMA = None
__PROPERTIES = None # A dict of parameter settings
__PROPFILE = "commondefs.yaml" # the default name of a YAML config file
__PROPSECT = "COMMONDEFS" # the default section of the YAML file
__LOGFILE = None
__LOGNAME = "ukge.log"
__LOGGER = None

DataSourceType = collections.namedtuple("DataSourceType",
                                        "isrdb table schema")
# Property: SourceData
#   Named tuple to hold input parameter data
SourceData = DataSourceType(False, None, None)

def set_dbase(db):
    global __DBASE
    __DBASE = db

def get_dbase():
    return __DBASE

def set_schema(sch):
    global __SCHEMA
    __SCHEMA = sch

def get_schema():
    return __SCHEMA

def set_propfile(fname):
    global __PROPFILE
    __PROPFILE = fname

def get_propfile():
    return __PROPFILE

def set_props(adict):
    global __PROPERTIES
    if isinstance(adict, dict):
        __PROPERTIES = adict

def get_propsect():
    return __PROPSECT
        
def get_props():
    return __PROPERTIES

def set_log(afile=__LOGNAME):
    global __LOGFILE, __LOGGER
    if not afile is None:
        __LOGFILE = afile
    else:
        __LOGFILE = __LOGNAME
    print(f"Logging to {__LOGFILE}")
    __LOGGER = logging.getLogger(__LOGFILE) # Get an instance
    __LOGGER.setLevel(logging.INFO)# Set lowest level to log
    __LOGGER.addHandler(logging.FileHandler(filename=afile)) # Attach a handler

def info_log(msg):
    if not __LOGFILE is None:
        __LOGGER.info(msg)
        
def error_log(msg):
    if not __LOGFILE is None:
        __LOGGER.error(msg)


class CommonDefs:
    """! @brief Common properties and methods to be used by all classes
    """
    def __init__(self):
        set_log() # Set name for log file
        props = self.readprops()
        if not isinstance(props,dict):
            info_log(f"CommonDefs: failed to read properties: {props}")
            sys.exit(f"CommonDefs: failed to read properties: {props}")
        set_props(props)
        # Assign some default values
        set_dbase(props["PgDBase"])
         

    #-------------------------------------------------------------------------#

    def readprops(self):
        """! @brief Read system properties from YAML properties file
        selects the 'metadata' section and returns
        this.  
        
        @returns Section of YAML file specified by _PROPERTIES_YSEC 
        or None on failure
        """
        # Read system properties from YAML properties file
        yaml = YAML(typ="safe", pure=True)
        print(f"Config file: {get_propfile()}")
        try:
            with open(get_propfile(), "r", encoding="utf-8") as f:
                p = yaml.load(f)
        except:
            sys.exit(f"readprops: Error reading properties file: {get_propfile()}")
            return None
        # I'm just interested in the relevant section of p
        sec = p[get_propsect()]
        info_log(f"readProps: Successfully read config file")
        if isinstance(sec,list):
            sec = sec.pop()
        return sec

 

