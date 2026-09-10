# file example.py

__DEFAULT_TYPE = "metadata"


class DBUtils:

    def __init__(self, dbtype=__DEFAULT_TYPE):
        """! @brief Constructor for DBUtils class """
        self.parms = get_props()
        self.df_varlist = None # basket variables

# Is the name __DEFAULT_TYPE well defined in the definition of the constructor __init__?
