# Metadata Extract for SPSS and STATA datasets
# Creator: Imran Shah
# Date 11th June 2018
# Arguments required to execute job
# 1. Full path to SPSS/STATA dataset.
# 2. Full path to output directory


from os import getenv
from tkinter import *
import csv
import time
import savReaderWriter as spss
import pandas as pd
import locale
import sys
import numpy as np
import os
from itertools import chain
import pandas as pd
import pyodbc
import psycopg2
import pymysql
from sqlalchemy import create_engine
import sqlite3
from sqlite3 import OperationalError

card = sys.argv[1]
homedir = "W:/metadata_ingest"
varlabs = homedir + '/' + card + '/' + card + '_variablelabels.csv'
vallabs = homedir + '/' + card + '/' + card + '_valuelabels.csv'
time = time.strftime('%Y-%m-%d %H:%M:%S')  # creating the time variable.

######################################################################################################################
# Check all the stuff that is a show stopper
def doChecks():
    global engine, cnxn, cursor, conn, engine2
#    global bskname, path1, path2, cursor, conn, cur, cnxn, cursor2, cnxn2

    # Check correct number of arguments provided when executing the script
    if len(sys.argv) < 2 or len(sys.argv) > 2:
        print("Incorrect number of arguments, please provide the full path to the dataset and the library card number")
        quit()

    # Connect to swift database
    try:
        cnxn = pyodbc.connect(
            "DRIVER={MySQL ODBC 5.3 Unicode Driver}; SERVER=localhost;DATABASE=swift_prod_10; UID=root;"
            " PASSWORD=m1a9r4c6h;" "local-infile=1")
    except:
        print("Unable to connect to the SWIFT database")
        quit()
    cursor = cnxn.cursor()

    # Second connection to the SWIFT database
    try:
        engine = create_engine('mysql+pymysql://root:m1a9r4c6h@localhost/swift_prod_10', echo=False)
    except:
        print("Unable to connect to the SWIFT database 2")
        quit()

    # Connect to postgres database
    try:
        conn = psycopg2.connect("dbname='nshd' user='postgres' host='swantest' password='m1a9r4c6h'")
        engine2 = create_engine('postgresql+psycopg2://postgres:m1a9r4c6h@swantest/nshd')
    except:
        print("Unable to connect to the postgres database")
        quit()
    cur = conn.cursor()

    # Check if CardNumber already exists in the filepath table

    sql = ("SELECT CardNumber, COUNT(*) FROM filepath WHERE CardNumber = '%s' GROUP BY CardNumber" % card)
    cursor.execute(sql)
    # gets the number of rows affected by the command executed
    row_count = cursor.rowcount
    if row_count > 0:
        print("\n The card number '"+ card + "', already exists in the filepath table")
        quit()

    # Check if CardNumber already exists in the variablelabel table

    sql = ("SELECT CardNumber, COUNT(*) FROM variablelabels WHERE CardNumber = '%s' GROUP BY CardNumber" % card)
    cursor.execute(sql)
    # gets the number of rows affected by the command executed
    row_count = cursor.rowcount
    if row_count > 0:
        print("\nThe card number '" + card + "', already exists in the variablelabels table")
        quit()

    # Check if variable does not already exists in the variablelabel table

    with open(varlabs, 'r') as csvfile:
        csvreader = csv.reader(csvfile)
        for row in csvreader:
            sql = ("SELECT Name, COUNT(*) FROM variablelabels WHERE Name = '%s' GROUP BY Name" % row[1])
            cursor.execute(sql)
            row_count = cursor.rowcount
            if row_count > 0:
                print("\nThe variable '"+row[1] + "', already exists in the variablelabels table")
                quit()

    # Check all variables have at least one category assigned to it
    with open(varlabs, 'r') as csvfile:
        csvreader = csv.reader(csvfile)
        for row in csvreader:
            if row[7] in (None, ""):
                print("\nAll variables must be assigned to one or more categories")
                quit()

    # Check all variables have a variable label
    with open(varlabs, 'r') as csvfile:
        csvreader = csv.reader(csvfile)
        for row in csvreader:
            if row[2] in (None, ""):
                print("\nAll variables must have a variable label")
                quit()

####################################################################################################################
# Uploading data
def uploadData():
    data = pd.read_csv(homedir + '/' + card + "/" + card + '_data.csv', sep=',', header='infer')
    data.to_sql(name=str.lower(card), con=engine2, index=False, if_exists='replace')#saving the dataset into the postgres table named after the card number



# Uploading variable metadata

def uploadMeta():
    print('\nUpdating the variablelabels table.................')
    data = pd.read_csv(varlabs, sep=","
                       , header='infer', usecols=['CardNumber', 'Name', 'Label', 'Form', 'QuestionNumber', 'Year',
                                                  'Derived', 'Creator', 'CreatorApp', 'Public'])
    data = data[data.Name != 'serno']
    data['CreateDate'] = time
    data['CreateDate'] = data['CreateDate'].astype('datetime64[ns]')

    data = data[['CardNumber', 'Name', 'Label', 'Form', 'QuestionNumber', 'Year', 'Derived', 'Creator', 'CreateDate',
                 'CreatorApp', 'Public']]

    data.to_sql(name='variablelabels', con=engine, if_exists='append', index=FALSE)

    # Uploading category membership labels

    print('\nUpdating the categorymembers table.................')
    data = pd.read_csv(varlabs, sep=","
                       , header='infer', usecols=['Name', 'Category', 'Creator', 'CreatorApp'])
    data = data[data.Name != 'serno']  # Remove the serno variable
    data = data.rename(columns=({'Category': 'code'}))  # Change the variable name to match the MySQL DB
    data['CreateDate'] = time
    data['CreateDate'] = data['CreateDate'].astype('datetime64[ns]')
    data['code'] = data.code.astype(str)

    data = data[['Name', 'code', 'Creator', 'CreateDate', 'CreatorApp']]

    a0 = []
    a1 = []
    a2 = []
    a3 = []
    a4 = []

    for index, row in data.iterrows():
        for s in row[1].split("+"):
            a0.append(row[0])
            a1.append(s)
            a2.append(row[2])
            a3.append(row[3])
            a4.append(row[4])

    data_cat = pd.DataFrame({'Name': a0, 'code': a1, 'Creator': a2, 'CreateDate': a3, 'CreatorApp': a4})

    data_cat.to_sql(name='categorymembers', con=engine, if_exists='append', index=FALSE)

    # Uploading Value labels
    print('\nUpdating the valuelabels table.................')

    data = pd.read_csv(vallabs, sep=","
                       , header='infer', usecols=['Name', 'Value', 'Label', 'MissingValueCode', 'Creator', 'CreatorApp'])
    data = data[data.Name != 'serno']  # Remove the serno variable
    data['CreateDate'] = time
    data['CreateDate'] = data['CreateDate'].astype('datetime64[ns]')

    data.to_sql(name='valuelabels', con=engine, if_exists='append', index=FALSE)

    # Uploading filepath table
    print('\nUpdating the filepaths table.................')

    data = pd.DataFrame(columns=['CardNumber', 'path', 'SERNO', 'Creator', 'CreateDate', 'CreatorApp'])
    data.loc[-1] = [card, 'Postgres table '+card, '', 'PC/IS/AM', time, 'Python']
    data['CreateDate'] = data['CreateDate'].astype('datetime64[ns]')
    data.to_sql(name='filepath', con=engine, if_exists='append', index=FALSE)


def descriptive():
    data = pd.read_csv(varlabs, sep=","
                       , header='infer', usecols=['CardNumber', 'Name', 'Label', 'Form', 'QuestionNumber', 'Year',
                                                  'Creator', 'CreatorApp', 'Public'])

    for index, row in data.iterrows():
        sqlhtml = ("load data local infile " + "\'" + homedir + '/' + card + "/" + row[1] + ".html" + "' "
                   "replace into table descriptives " +
                   "fields terminated by '/Z' " +
                   "enclosed by '' " +
                   "escaped by '' " +
                   "lines starting by '' " +
                   "terminated by '' " +
                   "(descriptives) " +
                   "SET NAME = " + "'" + row[1] + "'")
        print("\nUploading descriptives for " + row[1])
        try:
            cursor.execute(sqlhtml)
            cursor.commit()
        except pyodbc.Error:
            print("Error in uploading descriptives for " + row[1])


if __name__ == "__main__":
    doChecks()  # Performing the check
    uploadMeta()  # Upload the metadata
    descriptive()
#    uploadData()  # Upload the data
    print('\nUploading complete')
    print('\nPlease use STAT transfer to move the data to the postgres database')



