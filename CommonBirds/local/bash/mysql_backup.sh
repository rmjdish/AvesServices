#!/bin/bash
# Shell script to backup MySQL databases on Kiwi or Finch to a file space
# This script assumes you've set up a .mysql folder with login-paths
# This script uses Oracle MySQL client authentication style NOT Mariadb

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
declare -a dependencies=($(which mysqldump) $(which mysql) )
declare -r mysqlcreds=~/.mylogin.cnf
# Check we have the systems in place to do this
for dependency in ${dependencies[@]}; do
    if [ ! -x $dependency ]
    then
	echo "ERROR: Missing $dependency"
	exit 100
    fi
done			 
# Check there is a mysql config file
if [[ ! -f ${mysqlcreds} ]]
then
    echo "ERROR: Missing MySQL Credentials"
    exit 101
fi

echo 
# Source and destination servers
declare -r kiwi="mrc-kiwi01.ad.ucl.ac.uk"
declare -r finch="mrc-finch01.ad.ucl.ac.uk"
declare destpath="/xnat/san/backups" # 
declare HOST="kiwi"
declare MyDB="robin"
# Check for parameters
if [ $# -eq 1 ] # test for arithmetic equality
then
      MyDB="$1"
elif [ $# -eq 2 ]
then
    MyDB="$1"
    HOST="$2"
else
    echo "Usage: $0 [<db>] [kiwi|finch]"
    echo "Defaults are db=${MyDB} host=${HOST}"
fi
# copy from robin to rook
declare -r DMPDEST="${destpath}/${HOST}-${MyDB}.sql"
echo "Backing up ${MyDB} from ${HOST} to file ${DMPDEST}"
mysqldump --login-path="${HOST}" --opt "${MyDB}" > "${DMPDEST}"
echo "Finished backing up database from ${HOST} to ${DMPDEST}."
echo $(ls -lh "${DMPDEST}")



