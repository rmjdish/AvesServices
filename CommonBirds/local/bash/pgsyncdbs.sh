#!/bin/bash
# Script to copy a db on donkey to here (colt)
# Get ssh conn to donkey and execute pg_dump pumped through bzip2
# Pipe the result to bunzip2 and then onto psql - that's the theory!
declare -r srchost=donkey.local
declare -r tgthost=colt.local
if [[ $# -lt 1 ]]
then
    echo "Usage: pgsyncdbs <database name>"
    exit 1
fi
declare -r dbname=$1
echo "Trying to copy ${dbname} from Postgres on ${srchost} to Postgres on ${tgthost}"
# Give it a go
ssh  "${srchost}" "pg_dump --host=localhost --username=postgres --dbname=${dbname} --clean | bzip2" | bunzip2 | psql -h "${tgthost}" -U postgres "${dbname}"
