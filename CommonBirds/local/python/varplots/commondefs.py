"""! @brief This is a holding pen for def'ns common across the app
"""
import collections
import logging

__all__ = ["sortdict", "SourceData", "set_source", "get_source",
           "set_schema", "get_schema", "set_props", "get_props",
           "set_filename", "get_filename", "set_log", "info_log", "error_log"]


def sortdict(adict):
    """! @brief A utility function (not a method) to sort
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


__SOURCE = None
__SCHEMA = None
__FILENAME = None
__PROPERTIES = None
__LOGFILE = None
__LOGNAME = "metaplot"
__LOGGER = None

DataSourceType = collections.namedtuple("DataSourceType",
                                        "isrdb table schema file")
# Property: SourceData
#   Named tuple to hold input parameter data
SourceData = DataSourceType(False, None, None, None)

def set_source(srs):
    global __SOURCE
    __SOURCE = srs

def get_source():
    return __SOURCE

def set_schema(sch):
    global __SCHEMA
    __SCHEMA = sch

def get_schema():
    return __SCHEMA

def set_filename(fname):
    global __FILENAME
    __FILENAME = fname

def get_filename():
    return __FILENAME

def set_props(adict):
    global __PROPERTIES
    if isinstance(adict, dict):
        __PROPERTIES = adict
        
def get_props():
    return __PROPERTIES

def set_log(afile):
    global __LOGFILE, __LOGGER
    __LOGFILE = afile
    __LOGGER = logging.getLogger(__LOGNAME) # Get an instance
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
        self.logf = None
        self.props = None


