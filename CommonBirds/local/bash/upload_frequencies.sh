#!/bin/bash
# Shell script to upload CSV frequencies file to  MySQL table of the same name
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
declare -a dependencies=( $(which mysql) )
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
# Check for the existence of the frequencies file
declare csvfile=/xnat/san/SST/var_cell_counts/frequencies.csv
if [[ ! -f ${csvfile} ]]
then
    echo "ERROR: Missing frequencies CSV file ${csvfile}"
    exit 102
fi
# Create a temporary MySQL script to be executed
declare mysqlscript=/tmp/frequencies_upload.sql
declare optquote='"'
declare thetable="robin.frequencies"
declare mycreds="${HOME}/.mysql/mysql.cnf"
echo "-- 'WARNING! Truncates table ${thetable} and then uploads CSV file ${csvfile}'" > "${mysqlscript}"
echo "TRUNCATE TABLE ${thetable};" >> "${mysqlscript}"
echo "LOAD DATA LOCAL INFILE '${csvfile}'" >> "${mysqlscript}"
echo "REPLACE INTO TABLE ${thetable}" >> "${mysqlscript}"
echo "CHARACTER SET 'utf8'" >> "${mysqlscript}"
echo "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '${optquote}'" >> "${mysqlscript}"
echo "IGNORE 1 LINES" >> "${mysqlscript}"
echo "(id, fieldname, @fval, fieldcount)" >> "${mysqlscript}"
echo "SET fieldvalue = IF (@fval = '',-10, @fval);" >> "${mysqlscript}"
# Execute the script with the file just generated
mysql  --defaults-file="${mycreds}" --login-path=kiwi   < "${mysqlscript}"
echo "Uploaded ${csvfile} to frequencies table ${thetable} on Kiwi"
