#!/usr/bin/bash
# Shell script to replicate MySQL a specific table in dshare on Kiwi to Finch
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
echo Testing version CHANGE BEFORE RUNNING ON SwanTest!!!
# Source and destination servers
declare -r kiwi="mrc-kiwi01.ad.ucl.ac.uk"
declare -r finch="mrc-finch01.ad.ucl.ac.uk"
declare tgtpath="finch" # For testing only!!! CHANGE ME TO FINCH
# setut up an array of tables
declare -a tables=(
'projects'
)
echo "Have ${#tables[@]} to copy from$ ${kiwi}/dshare  to ${finch}/dshare"
# iterate through the array of tables
declare -i index=0 # local integer variable
for tab in "${tables[@]}"
do
    nf=$(( ${nf} + 1)) # double brackets for integer arithmetic
    # copy from kiwi/dshare to finch/dshare
    echo "Copying ${tab} from ${kiwi}/dshare to ${tgtpath}/dshare"
    mysqldump --login-path=kiwi dshare "${tab}" \
	| mysql --login-path="${tgtpath}" dshare
    let "index+=1" # nice incremental statement
done
echo "Copyied $((${index} + 1)) tables."



