#!/usr/bin/bash
# Shell script to build a NSHD basket using Python nshd package
# It is used by PyJobService Boomerang class to remotely build baskets
# This script needs 2 things to work: the basket name, the user name of the basket owner
#
if [ ! $# -eq 2 ]; then
    echo "Usage: $0 <basket name> <user name of basket owner>"
    exit 1
fi
declare -r basketname=$1
declare -r username=$2
declare -r VENVWRAPPER_HOME=/usr/local/bin/virtualenvwrapper.sh
declare -r VENV_NAME=baskets
# Make virtual environment work
source "${VENVWRAPPER_HOME}"  
workon "${VENV_NAME}"
buildbasket "${basketname}" "${username}"