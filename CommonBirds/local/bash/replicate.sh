#!/bin/bash
# Shell script to replicate MySQL table to the correct servers
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
declare mysqlcreds=~/.mysql/mysql.cnf
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
# setut up an array of tables
declare -a tables=(
'categorylabels'
'categorymembers'
'descriptives'
'docs'
'filepath'
'frequencies'
'keywords'
'longitudinalvars'
'mapping_table'
'mapping_valuelabels'
'messages'
'pgtable_cardnumber_diffs'
'scalevars'
'scrambling'
'shadowvariables'
'valuelabels'
'variablelabels'
'varsecmod')
echo "Have ${#tables[@]} to copy from kiwi.robin to kiwi.rook and finch.robin"
kiwi="mrc-kiwi01.ad.ucl.ac.uk"
finch="mrc-finch01.ad.ucl.ac.uk"
# iterate through the array of tables
let index=0 # local integer variable
for tab in "${tables[@]}"
do
    nf=$(( ${nf} + 1)) # double brackets for integer arithmetic
    # copy from robin to rook
    echo "Copying ${tab} from ${kiwi}/robin to ${kiwi}/rook"
    mysqldump --login-path=kiwi robin "${tab}" \
	| mysql --login-path=kiwi rook
    # copy from kiwi/robin to finch/robin
    echo "Copying ${tab} from ${kiwi}/robin to ${finch}/robin"
    mysqldump --login-path=kiwi robin "${tab}" \
	| mysql --login-path=finch robin
    let index++ # nice incremental statement
done
echo "Copyied $((${index} + 1)) tables."

