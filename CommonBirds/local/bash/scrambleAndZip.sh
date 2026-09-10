#!/bin/bash
# Shell script to scramble an already built NSHD basket using Python nshd package
# It is used by PyJobService Boomerang class to remotely scramble baskets
# This script needs 2 things to work: the basket name, the scramble code user name (Project scramble user)
#
if [ ! $# -eq 2 ]; then
    echo "Usage: $0 <basket name> <user name from scramble db>"
    exit 1
fi
# Read only variables
declare -r BSKHME=/xnat/san/SST/baskets
declare -r PYSCPT=/opt/swift/sdms.py
declare -r PYARG1="scramble"
declare -r PYARG2="CSV"
declare -r BASKET=$1
declare -r PYARG4="SERNO"
declare -r SCRCDE=$2
declare -r TGTDIR="${BSKHME}/${BASKET}/scrambled"
declare -r ZIPOUT="${TGTDIR}/${BASKET}-SCRAMBLED.zip"
declare -r TMPZIP="/tmp/$(basename ${ZIPOUT}).$$"

# Execute the python script to create all the necessary files
python "${PYSCPT}" "${PYARG1}" "${PYARG2}" "${BASKET}" "${PYARG4}" "${SCRCDE}"
# ZIP is temperamental so move to where files are
cd "${TGTDIR}" || exit 1
zip -j -r "${TMPZIP}" ./* -x "$(basename ${ZIPOUT})" || exit 1
# Copy avoids rename problems
cp -f "${TMPZIP}" "${ZIPOUT}"
rm -f "${TMPZIP}"
