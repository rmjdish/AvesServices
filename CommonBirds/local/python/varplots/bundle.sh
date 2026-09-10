#!/bin/bash
# Bash script to wrap up metaplot into tar file
echo "Varplots Package $(date)" > version.txt
progs=('commondefs.py' 'datapull.py' 'dbutils.py' 'metaconfig.yaml' 'metaplot.py' 'plotcolumn.py' 'varplots.py' 'writeplot.py')
htmldir='./html'
# Write all file cheksums to the vesion file
for ele in "${progs[@]}"
do
    sha256sum "${ele}" >> version.txt
done
for file in "${htmldir}"/*.html
do
	sha256sum  "${file}" >> version.txt 
done
# Create the tar file
tar --create --file varplots.tar version.txt
for ele in "${progs[@]}"
do
    tar --file varplots.tar --append "${ele}" >> version.txt
done
# Add html dir
tar --file varplots.tar --append "${htmldir}" >> version.txt
# List the contents of the archive
tar --file varplots.tar --list
