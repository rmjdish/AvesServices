#!/bin/bash
# Bash script to copy CSV files generated from PostgreSQL to large CIS share
source=/tmp/
dest=/xnat/san/SST/var_cell_counts/
cp -fvub "$source"/*.csv "$dest"
# Also upload the CSV frequencies file to robin.frequencies
/opt/scripts/upload_frequencies.sh
