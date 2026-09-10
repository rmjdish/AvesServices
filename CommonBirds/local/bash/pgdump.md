pgdump.sh 1 "May 2025" pgdump.sh "User Manual"
================================================

[//]: # (This is a markdown file for use with go-md2man)
[//]: # (The format of the heading lines is:)
[//]: # (<command name> <man no> <date> <command_name> "User Manual")
[//]: # (With a sequence of equal chars for the top heading)


## NAME
pgdump.sh - backup PostgreSQL database to text file

## SYNOPSIS

pgdump.sh _dbname_ 

## DESCRIPTION

This program uses the _pg\_dump_ software to generate an encrypted
backup file that can be used to restore the database if required.

## ENVIRONMENT

The backup file will be written to the folder _/xnat/san/SST/backups/_. The file name includes the name of the database and the current date.

## AUTHOR
Phil Curran

## COPYRIGHT

All copyright is reserved by the authors.  This software is released under the GPL version 3 software license.

## SEE ALSO
pg\_dump(1)

