"""
@brief Module to transform long CSV files into wide CSV files and vice versa
@file transform.py
"""
import sys
import json
import csv
import copy
import datetime
from pathlib import Path
from functools import reduce

class TransException(Exception):
    """! @brief Application level exception
    """
    def __init__(self, message : str):
        super().__init__(message)
        self.timestamp = datetime.datetime.now()
        

class GenTrans:
    """! @brief Transpose CSV file table
    """
    __CURRENT_ID = None
    __PRIMARY_KEY = None
    __CONFIG_PARMS = None


    def __init__(self,jsonfile : str):
        """! @brief Set the config file path
        @parm jsonfile - String corresponding to the config file path
        """
        jfile = Path(jsonfile)
        if jfile.exists():
            self.jsonfile = jfile
        else:
            self.jsonfile = None
        self.primary_key = None
        self.direction = None

    def set_jsonfile(self, jfile: str):
        jf = Path(jfile)
        if jf.exists():
            self.jsonfile = jfile
        else:
            raise TransException(f"Invalid JSON file: {jfile}")
            self.jsonfile = None
        return self.jsonfile
    
    def get_jsonfile(self):
        return self.jsonfile
        
    def get_parms(self) -> dict:
        return GenTrans.__CONFIG_PARMS

    def set_parms(self, adict: dict):
        if isinstance(adict, dict):
            GenTrans.__CONFIG_PARMS = adict
        else:
            raise TransException(f"Not Parms Dictionary  {adict}")
            
        
    def set_current_id(self, akey : str):
        if isinstance(akey, str):
            GenTrans.__CURRENT_ID = akey
        else:
            raise TransException(f"Expect String ID {akey}")

    def get_current_id(self):
        return GenTrans.__CURRENT_ID

    def set_direction(self, direc : str):
        if isinstance(direc, str):
            self.direction = direc
        else:
            raise TransException(f"Expect Direction as String {direc}")

    def get_direction(self) -> str:
        if isinstance(self.get_parms(), dict):
            return self.get_parms()["direction"]
        else:
            raise TransException(f"Expect Parms as Dictionary {self.get_parms()}")
        
    
    def process_batch(self):
        pass

    def read_json(self):
        with open(self.get_jsonfile()) as jfile:
            adict = json.load(jfile)
            self.set_parms(adict)
            
    def json_set_parms(self):
        if isinstance(self.get_parms(), dict):
            pass
        else:
            raise TransException(f"Invalid parms {self.get_parms()}")

            
    def get_long_batch(self, filename: str):
        batch = []
                    
    def get_primary_key(self):
        if isinstance(self.get_parms(), dict):
            return self.get_parms()["primary_key"]
        else:
            raise TransException(f"Cannot get primary key from {self.get_parms()}")

    def get_input_file(self):
        if isinstance(self.get_parms(), dict):
            return self.get_parms()["input"]
        else:
            raise TransException(f"Cannot get input file from {self.get_parms()}")

    def get_output_file(self):
        if isinstance(self.get_parms(), dict):
            return self.get_parms()["output"]
        else:
            raise TransException(f"Cannot get output file from {self.get_parms()}")

    def get_static_cols(self):
        if isinstance(self.get_parms(), dict):
            return self.get_parms()["static_cols"]
        else:
            raise TransException(f"Cannot get static cols from {self.get_parms()}")

    def get_transform_cols(self):
        if isinstance(self.get_parms(), dict):
            return self.get_parms()["transform_cols"]
        else:
            raise TransException(f"Cannot get transform cols from {self.get_parms()}")

    def lolcar(self, lol : list) -> list:
        """! @brief List of lists car
        @parm lol - A list of lists
        @returns List - the list of all first elements of sublists of lol
        """
        results = list()
        if isinstance(lol, list):
            for subl in lol:
                if isinstance(subl, list):
                    tmplst = copy.deepcopy(subl)
                    results.append(tmplst.pop(0))
                else:
                    raise TransException(f"Expect List of Lists {lol}")
                    
        return results

    def car(self, lst : list) -> list:
        """! @brief Good old Lisp car - list head
        @parm lst - A list
        @returns Element - the first element from lst
        """
        result = None
        if isinstance(lst, list):
            tmplst = copy.deepcopy(lst)
            result = tmplst.pop(0)
        else:
            raise TransException(f"Expect List {lst}")
        return result

    def cdr(self, lst : list) -> list:
        """! @brief Good old Lisp cdr - list remainder
        @parm lst - A list
        @returns List - the list constructed by removing the first element from lst
        """
        results = list()
        if isinstance(lst, list):
            tmplst = copy.deepcopy(lst)
            tmplst.pop(0)
            results = tmplst
        else:
            raise TransException(f"Expect List {lst}")
        return results

    def get_col_suffixes(self):
        dictlist = self.get_transform_cols()
        if isinstance(dictlist, list):
            return [ x['long_values'] for x in dictlist ] 
        else:
            raise TransException(f"Cannot extract long_values column names from {get_transform_cols()}.")
        

    def print_state(self):
        print("Transform:\n==================")
        print(f"JSON File: {self.get_jsonfile()}")
        print(f"Primry Key: {self.get_primary_key()}")
        print(f"Input File: {self.get_input_file()}")
        print(f"Output File: {self.get_output_file()}")
        print(f"Static Cols: {self.get_static_cols()}")
        print(f"Transform Cols: {self.get_transform_cols()}")
        print("\n\n")


    def get_uniques(self) -> list:
        uniqs = dict() 
        cols = self.get_transform_cols() # returns list of lists
        cols = [ set(x) for x in cols ] # list of sets
        cols = reduce(lambda x,y: x.union(y), cols) # flatten sets
        if len(cols) > 0:
            with open(self.get_input_file(), newline='') as csvf:
                dictreader = csv.DictReader(csvf, delimiter=',')
                for row in dictreader:
                    column_list = row.keys()
                    if dictreader.line_num == 2:
                        uniqs = dict.fromkeys(cols) # dict with all cols as key and None as all values
                        print(f"get_uniques: created dict uniqs: {uniqs}")
                        if any([ not x in column_list for x in cols ]):
                            return None
                        else:
                            for col in cols:
                                uniqs[col] = {row[col]}
                    # update all columns in cols
                    for col in cols:
                        uniqs[col].add(row[col])
                # End of CSV File
                return uniqs
            
    def check_input(self):
        uniqs = self.get_uniques() # returns dict of cols with unique values as elements
        new_cols = dict()
        with open(self.get_input_file(), newline='') as csvf:
            dictreader = csv.DictReader(csvf, delimiter=',')
            rowcnt = 0
            for row in dictreader:
                existing 
                rowcnt += 1
                print(f"\n\nRow {rowcnt} => {row}")
                # put in primary key and all static cols
                print(f"\t\tTransform Columns: {transform_cols}")
                for dic in transform_cols:
                    for key,value in dic.items():
                        # Get the value of dic[value] this becomes new row[key]
                        f_pair = tuple(value.split(", "))
                        col_name = '_'.join(f_pair)
                        print(f"\t\tkey: {key}, value: {value}, col_name: {col_name}, row[f_pair[1]]: {row[f_pair[1]]}")
                        new_cols.update({ col_name: row[f_pair[1]] })
                        print(f"\t\tNew Columns: {new_cols}")
                if rowcnt >= 10:
                    break

        
    def go_wide(self):
        """! @brief Transform CSV to wide format
        @details The problem is to read all entries for primary id
        and create columns 
        """
        with open(self.get_input_file(), newline='') as csvf:
            dictreader = csv.DictReader(csvf, delimiter=',')
            rowcnt = 0
            for row in dictreader:
                if row[primary_key] == trans.get_current_id():
                    rowcnt += 1
                    # put in primary key and all static cols
                    
                    for dic in transform_cols:
                        for key,value in dic.items():
                            new_cols[row[dic["long_column"]]] = row[dic["long_values"]]
                elif trans.get_current_id() is None:
                    # Start of loop
                    trans.set_current_id(row[primary_key])
                    rowcnt = 0
                else:
                    # End of batch
                    print(f"ID {trans.get_current_id()} has {rowcnt} records.")
                    print(f"New columns: {new_cols}")
                    trans.set_current_id(row[primary_key])
                    rowcnt = 0
                    
            # got to the end
            print(f"ID {trans.get_current_id()} has {rowcnt} records.")

    def go_long(self):
        """! @brief Transform CSV to long format
        """
        cols = [self.get_primary_key()]
        cols += self.get_static_cols()
        cols += self.lolcar(self.get_transform_cols())
        new_record = dict.fromkeys(cols)
        new_cols = self.lolcar(self.get_transform_cols())
        mask = dict.fromkeys(new_cols, False) # A mask on the new record
        equiv_map = dict()
        for lst in self.get_transform_cols():
            lstcar = self.car(lst)
            lstcdr = self.cdr(lst)
            lstmap = { x: lstcar for x in lstcdr }
            equiv_map.update(lstmap)
        print(f"New CSV Columns: {cols}")
        print(f"Mapping From: {set(equiv_map.keys())}")
        all_records = []
        with open(self.get_input_file(), newline='') as csvf:
            dictreader = csv.DictReader(csvf, delimiter=',')
            rowcnt = prncnt = 0
            for row in dictreader:
                rowcnt += 1
                new_record = dict.fromkeys(cols) # All values are None
                mask = dict.fromkeys(new_cols, False) # Clear at start of record
                for key, value in row.items():
                    if key in new_record:
                        new_record[key] = value
                    elif key in equiv_map:
                        new_record[equiv_map[key]] = value
                        if value != '':
                            mask[equiv_map[key]] = True
                    else:
                        print(f"Column not found: {key} at {rowcnt}")
                    # New record full yet?
                    if all(map(lambda x: mask[x], new_cols)):
                        print(f"**** Output: {new_record}")
                        all_records.append(copy.deepcopy(new_record)) # Add to list
                        prncnt += 1
                        mask = dict.fromkeys(new_cols, False) # Clear at start of record
                # This is the end of this row so output anything left
                if any(map(lambda x: mask[x], new_cols)):
                    all_records.append(copy.deepcopy(new_record)) # Add to list
                    print(f"**** Output: {new_record}")
                    prncnt += 1
        # Finished Reading Input CSV File - Now Output 
        with open(self.get_output_file(), 'w', newline='') as outf:
            dictwriter = csv.DictWriter(outf, fieldnames=new_record.keys(), delimiter=',')
            dictwriter.writeheader()
            dictwriter.writerows(all_records)
        print(f"Source file records: {rowcnt} / Output file records: {len(all_records)}")

    

        

