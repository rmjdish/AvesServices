import transform as tf
import pandas as pd
import string
import random

def pair_of_suffixes():
    keylen = 2
    # using random.choices() generating random strings
    return ('_'+''.join(random.choices(string.ascii_lowercase,k=keylen)),
            '_'+''.join(random.choices(string.ascii_lowercase,k=keylen)))
    

if __name__ == "__main__":
    trans = tf.LongWideTrans()
    trans.set_jsonfile("transform.json")
    trans.read_json()
    try:
        primary_key = trans.get_primary_key()
        input_file = trans.get_input_file()
        output_file = trans.get_output_file()
        static_cols = trans.get_static_cols()
        transform_cols = trans.get_transform_cols()
        suffix_list = trans.get_col_suffixes()
        trans.print_state()
    except TransException as err:
        print(f"Help!!! {err}")
        quit
    # Ok this far...
    long_df = pd.read_csv(input_file)
    print(f"Original DataFrame Columns: {long_df.columns}")
    print(f"Columns used for values: {suffix_list}")
    basic_df = long_df[static_cols] # Constrict to these columns
    # Need to pick primary key and drop all but one of long form duplicates 
    basic_df = basic_df.drop_duplicates(subset=[primary_key], keep='first')
    wide_ones  = [] # Used to collect all pivot DataFrames
    print("Basic DataFrame:\n================\n", basic_df)
    for trf in transform_cols:
        # This will be a dict
        #print("Working on:", trf)
        long_col = trf.pop('long_column')
        long_val = trf.pop('long_values')
        """ From the Pandas 2.2 documenttion for pivot:
        Parameters:
        
        columns : str or object or a list of str
            Column to use to make new frame’s columns.
        index :   str or object or a list of str, optional
            Column to use to make new frame’s index. If not given, uses existing index.
        values :  str, object or a list of the previous, optional
            Column(s) to use for populating new frame’s values. If not specified,
        all remaining columns will be used and the result will have hierarchically
        indexed columns.
        
        Returns:
            DataFrame (reshaped)
        Raises:
            ValueError:
            When there are any index, columns combinations with multiple values.
            DataFrame.pivot_table when you need to aggregate.
        """
        wide_df = long_df.pivot(index=primary_key, columns=long_col, values=long_val)
        wide_df = wide_df.reset_index(level=primary_key)
        wide_ones.append(wide_df) # save to list of DataFrames
    # Try znd put this all together
    print("Number of wide DataFrames to merge:",len(wide_ones))
    result = basic_df 
    for df in wide_ones:
        #print(f"New Wide DataFrame Columns: {df.columns}")
        suffix_list = pair_of_suffixes()
        print(f"Pair of suffixes used in merge: {suffix_list}")
        result = result.merge(right=df,
                              on=primary_key,
                              suffixes=suffix_list,
                              how='inner')
        print(f"Merged columns so far: {result.columns}")
    #print("Merge with original:\n", result)
    # Finally wrtie to CSV file
    try:
        print(f"Attempting to write above DataFrame to CSV file {output_file}")
        result.to_csv(output_file, index=False)
    except IOError as err:
        print(f"Error creating CSV output file: {err}")
        quit
    print("Done.")
