# ukge #

## A very basic package for accessing UK GE Data from Postgresql ##

This package allow easy importation of UK General Election and EU
referendum data from a Postgresql database called *genelects*.

It supports a single class level function **create_yaml_file** and the
**DataPull** class which has a number of instance methods.

Calls to **create_yaml_file** should preceed any instantiation of a
**DataPull** object.  This class level function creates the default YAML configuration file for use by the **DataPull** class.

## DataPull API ##

This is incredibly simple there are only a few methods:
  * create\_yaml\_file Class level function simply call it before you create a Dat-
aPull object and you don’t have to create the YAML file, this function will create the current one in your working directory
  * getPG Generic method. Requires instance variables self.schema and self.table
to be set first and will then return a Pandas dataframe consisting of the
data in that table
  * setSchema For use with getPG above. Takes one string argument the Post-
gres schema name. Not needed if you use any of the methods below.
  * setTable For use with getPG above. Takes one string argument the Postgres
table name. Not needed if you use any of the methods below.

### British General Election Data ###

  * pullGE2024 Returns a Pandas dataframe with 650 rows and 29 columns with
2024 UK GE data
  * pullGE2024meta Returns a Pandas dataframe of variable labels with columns
’fldname’ and ’fldlabel’
  * pullGE2019 Returns a Pandas dataframe with 650 rows and 35 columns with
2019 UK GE data
  * pullGE2019meta Returns a Pandas dataframe of variable labels with columns
’fldname’ and ’fldlabel’
  * pullGE2017 Returns a Pandas dataframe with 650 rows and 32 columns with
2017 UK GE data
  * pullGE2017meta Returns a Pandas dataframe of variable labels with columns
’fldname’ and ’fldlabel’
  * pullGE2015 Returns a Pandas dataframe with 650 rows and 31 columns with
2015 UK GE data
  * pullGE2015meta Returns a Pandas dataframe of variable labels with columns
’fldname’ and ’fldlabel’
  * pullGE2010 Returns a Pandas dataframe with 650 rows and 31 columns with
2010 UK GE data
  * pullGE2010meta Returns a Pandas dataframe of variable labels with columns
’fldname’ and ’fldlabel’
  * pullGE2005 Returns a Pandas dataframe with 646 rows and 23 columns with
2005 UK GE data
  * pullGE2005meta Returns a Pandas dataframe of variable labels with columns
’fldname’ and ’fldlabel’
  * pullGE2001 Returns a Pandas dataframe with 659 rows and 20 columns with
2001 UK GE data
  * pullGE2001meta Returns a Pandas dataframe of variable labels with columns
’fldname’ and ’fldlabel’
  * pullGE1997 Returns a Pandas dataframe with 660 rows and 30 columns with
1997 UK GE data
  * pullGE1997meta Returns a Pandas dataframe of variable labels with columns
’fldname’ and ’fldlabel’
  * pullBrexit Returns a Pandas dataframe with 650 rows corresponding to the
2010 constituency ids and names. The imputed percentage leave vote
for each constituency is contained in the field ref16 best.
