replicate\_dshare.sh 1 "May 2025" replicate\_deshare.sh "User Manual"
================================================

[//]: # (This is a markdown file for use with go-md2man)
[//]: # (The format of the heading lines is:)
[//]: # (<command name> <man no> <date> <command_name> "User Manual")
[//]: # (With a sequence of equal chars for the top heading)


## NAME
replicate\_deshare.sh - copies MySQL **dshare.projects** to Finch.

## SYNOPSIS

replicate\_deshare.sh

## DESCRIPTION

This program uses the MySQL software _mysqldump_ and the _mysql_ client program
to copy the **projects** table in the **dshare** database from Kiwi to Finch.

## ENVIRONMENT

This program requires the use to have set up valid MySQL credentials set up in the _~/.mylogin.cnf_ file.  It will not run unless this has been setup properly.


## AUTHOR
Phil Curran

## COPYRIGHT

All copyright is reserved by the authors.  This software is released under the GPL version 3 software license.

## SEE ALSO
To setup you MySQL credentials see **mysql-config-editor(1)**
