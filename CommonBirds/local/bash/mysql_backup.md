mysql\_backup.sh 1 "May 2025" mysql\_backup.sh "User Manual"
================================================

[//]: # (This is a markdown file for use with go-md2man)
[//]: # (The format of the heading lines is:)
[//]: # (<command name> <man no> <date> <command_name> "User Manual")
[//]: # (With a sequence of equal chars for the top heading)


## NAME
mysql\_backup.sh - backup MySQL database on Kiwi or Finch to a file

## SYNOPSIS

mysql\_backup.sh _db_ _host_ 


## DESCRIPTION

This program takes two arguments, the first (_db_) specifies the MySQL database to be backed up and the second (_host_) specifies the host on which the database is located. The resulting file is written to _/xnat/san/backups_ on mrc-swan01.ad.ucl.ac.uk

## ENVIRONMENT

The user must have setup valid credentials in the file _~/.mylogin.cnf_ using the MySQL utility mysql-config-editor.

The resulting file can be used to restore the database using the _mysql_ client.

## AUTHOR
Phil Curran

## COPYRIGHT

All copyright is reserved by the authors.  This software is released under the GPL version 3 software license.

## SEE ALSO
mysql(1), mysqldump(1)
