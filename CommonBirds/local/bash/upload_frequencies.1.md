upload\_frequencies.sh 1 "May 2025" upload\_frequencies.sh "User Manual"
================================================================================================

[//]: # (This is a markdown file for use with go-md2man)
[//]: # (The format of the heading lines is:)
[//]: # (<command name> <man no> <date> <command_name> "User Manual")
[//]: # (With a sequence of equal chars for the top heading)


## NAME

**upload_frequencies.sh** -  Shell script to upload CSV frequencies file to MySQL table of the same name


## SYNOPSIS

**upload_frequencies.sh** 


## DESCRIPTION

This command creates an SQL script to use the MySQL _LOAD DATA LOCAL
INFILE_ facility to upload the contents of the CSV file
_/xnat/san/SST/var\_cell\_counts/frequencies.csv_ to the table
**robin.frequencies**.

## ENVIRONMENT

As with other **bash** based programs that use MySQL utility programs,
the successful use of this program requires the use to have set up a
_.mysql_ credentials folder with login-paths defined for both the Kiwi
and Finch servers.

## AUTHOR
Phil Curran

## COPYRIGHT

All copyright is reserved by the authors.  This software is released
under the GPL version 3 software license.


