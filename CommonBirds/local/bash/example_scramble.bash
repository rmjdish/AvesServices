#!/bin/bash
python /opt/swift/sdms.py scramble CSV microgoldZZrteqtw SERNO DSH

# New bits
declare -r TGTDIR=/xnat/san/SST/baskets/microgoldZZrteqtw/scrambled
declare -r ZIPOUT="${TGTDIR}/microgoldZZrteqtw-SCRAMBLED.zip"
declare -r TMPZIP="/tmp/$(basename ${ZIPOUT}).$$"

cd "${TGTDIR}" || exit 1
zip -j -r "${TMPZIP}" ./* -x "$(basename ${ZIPOUT})" || exit 1
# Copy avoids rename problems
cp -f "${TMPZIP}" "${ZIPOUT}"
rm -f "${TMPZIP}"
