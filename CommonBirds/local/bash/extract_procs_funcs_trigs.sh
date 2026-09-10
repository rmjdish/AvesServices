#!/bin/bash
# Shell script to replicate MySQL procedures/functions/triggers  on Kiwi
# to the correct servers
# This script assumes you've set up a .mysql folder with login-paths
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
declare mysqlcreds=~/.mylogin.cnf
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

declare -r src="$1"

case "${src}" in
    "finch" | "kiwi" | "zebra")
        echo "Using ${src} to obtain procedure/function/trigger defs."
	declare outfile="${src}_procs_funcs_triggs.sql"
	echo "Output will be written to file: ${outfile}"
        ;;
    *)
        echo "I need a know source as parameter; I don't know the source provided."
	exit 102
        ;;
esac


# Source and destination servers
declare -r kiwi="mrc-kiwi01.ad.ucl.ac.uk"
declare -r finch="mrc-finch01.ad.ucl.ac.uk"
declare tgtpath="${src}" 
echo "Obtaining info from login-path ${tgtpath}"
# Try and dump it all
mysqldump --login-path="${tgtpath}" \
	  --routines \
	  --triggers \
	  --no-create-info \
	  --add-drop-trigger \
	  --no-data \
	  --no-create-db \
	  --skip-opt robin > "${outfile}"










