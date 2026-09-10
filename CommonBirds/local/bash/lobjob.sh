#!/usr/bin/bash
# Shell script to scramble an already built NSHD basket using Python nshd package
# It is used by PyJobService Boomerang class to remotely scramble baskets
# This script needs 2 things to work: the basket name, the scramble code user name (Project scramble user)
#
if [ ! $# -eq 2 ]; then
    echo "Usage: $0 <basket name> <user name from scramble db>"
    exit 1
fi
declare -r basketname=$1
declare -r username=$2
declare -r VENVWRAPPER_HOME=/usr/local/bin/virtualenvwrapper.sh
declare -r VENV_NAME=baskets
# Make virtual environment work
source "${VENVWRAPPER_HOME}"  
workon "${VENV_NAME}"
scramblebasket "${basketname}" "${username}"