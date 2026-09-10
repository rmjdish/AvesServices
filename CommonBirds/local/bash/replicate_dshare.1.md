replicate\_dshare.sh 1 "May 2025" replicate\_dshare.sh "User Manual"
===========================================================================================

[//]: # (This is a markdown file for use with go-md2man)
[//]: # (The format of the heading lines is:)
[//]: # (<command name> <man no> <date> <command_name> "User Manual")
[//]: # (With a sequence of equal chars for the top heading)

## NAME

**replicate\_dshare.sh** - Shell script to replicate MySQL a specific table in the database _dshare_ on Kiwi to Finch.


## SYNOPSIS

**replicate_dshare.sh**


## DESCRIPTION

This program takes no arguments.  The _dshare_ database on the Finch
server is used by the NSHD website to list the approved Data Access
Requests for research projects using NSHD birth cohort data.

## ENVIRONMENT

As with other **bash** based programs that use MySQL utility programs,
the successful use of this program requires the use to have set up a
_.mysql_ credentials folder with login-paths defined for both the Kiwi
and Finch servers.

## AUTHOR
Phil Curran

## COPYRIGHT

All copyright is reserved by the authors.  This software is released under the GPL version 3 software license.

