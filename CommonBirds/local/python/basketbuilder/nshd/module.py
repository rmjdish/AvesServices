#!/usr/bin/python
"""// Author:  Phil Curran
// Copyright (C) Phil Curran
// This software is released under the GPL version 2.0 license

 About: package nshd
 
 It creates a table in PG from the supplied basket provided it can
 find it in the basketdetails table.  It uses PG SQL to build the
 dataset and generate the CSV files.

 Useage: python basketbuilder.py <basketID> <username>

 This script takes two arguments: the name of the basket and the
 owner's username.  It will create a folder with this name if one does
 not already exist All built CSV and syntax files will be stored here
 with a time stamp as part of their name.

 This version expects arg1 = <basketID>, arg2 = <username> It also
 relies on a YAML configuration file which is set in one of the class
 constructor methods.

"""
import sys
from nshd.basketutils import BasketBuilder
from nshd.scramutils import Scramble
from nshd.commondefs import *
from nshd.fileutils import FileOps

################################################################################
#    Function: main_function
#        When called as a stand alone program this creates
#        an instance called *bob* and calls *thebuilder* method
#
#    Parameters:
#        - basket name
#        - user name of basket owner
def main_function():
    info_log(f"{ThisVersion()} Build dataset from basket {sys.argv[1]} using PostgreSQL NSHD Repository.")
    bob = BasketBuilder()
    info_log(f"{ThisVersion()} Files will be created with the time stamp of: {bob.thetime}")
    if bob.getIsa() == "basket":
        fops = FileOps()
        if fops.dataExists():
            # Nothing to do already built
            info_log(ThisVersion()+" Data files already exist; not building just zipping up")
        else:
            # Need to build it
            bob.thebuilder()
    elif bob.getIsa() == "trolley":
        bob.getscoop()
    info_log(f"{ThisVersion()} main_function: Finished building basket.")


def scram_function():
    info_log(f"{ThisVersion()} Scramble dataset from basket {sys.argv[1]} using existing built basket.")
    bob = Scramble()
    bob.thescrambler()
    info_log(f"{ThisVersion()} scram_function: Finished scrambling basket.")
        
    

if __name__ == "__main__":
    main_function()
###############################################################################
