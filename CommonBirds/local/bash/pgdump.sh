#!/bin/bash
# Script to backup nshd data repository from PostgreSQL db
# produces a compressed SQL text file
# Takes one argument: the name of the db to backup
pstfix=$(date +%F)
thisone=$1
outfile="/xnat/san/SST/backups/mrc-swan01_$thisone_$pstfix.bz.gpg"
echo Encrypted pg_dump backup file will be: $outfile
pg_dump -h localhost -U postgres "$thisone" | bzip2 | gpg --batch --symmetric --passphrase m1a9r4c6h --output "$outfile"
