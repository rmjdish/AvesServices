#!/bin/bash
# Script to copy files from /nfs/home and /nfs/home1 to /xnat/san/SST/HandQ
#### Error handling
function handle_error() {
  # Get information about the error
  local error_code=$?
  local error_line=$BASH_LINENO
  local error_command=$BASH_COMMAND

  # Log the error details
  echo "Error occurred on line $error_line: $error_command (exit code: $error_code)"

  # Optionally exit the script
  exit 1
}
set -o errtrace # This will abort the script if any part is unsuccessful (returns non-zero exit code)
trap handle_error ERR

#### Shell script to copy files from development environment to Apache
#### site location, copy the Python VE and restart the web server
# Need to have root permisisions to do this
if [[ $(whoami) != 'root' ]]
then
    echo "$0 : You need root permissions to run this!"
    exit 1
fi
#### site location, copy the Python VE and restart the web server
declare -r HOST=$(hostname -a|cut -d " " -f 1)
declare -r SHORTHOST=$(hostname)
declare -r SRCHOME=/nfs
declare -r TRGTDIR=/xnat/san/SST/HandQ
# Probably better to list individual folders rather than just top level
declare -a FOLDERS=(
    "${SRCHOME}/home"
    "${SRCHOME}/home1"
)
declare -r DSTHOME=/xnat/san/SST/HandQ
echo "================================================"
echo "$0 : Copying Files/Folders to ${DSTHOME}"
echo "================================================"
# change the group ownership on src folders to be copied
for flr in "${FOLDERS[@]}"
do
    echo "Changing group ownerhip on ${flr} to nshd"
    sudo chgrp -R nshd "${flr}"
done

# iterate through the array of folders
for flr in "${FOLDERS[@]}"
do
    echo "Copying from ${flr} to ${DSTHOME}/${flr}"
    rsync -nah --partial --info=progress2 "${flr}"  "${DSTHOME}"
done

echo "$0 : Finished rsyncing folder to "
