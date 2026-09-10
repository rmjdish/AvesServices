#!/bin/bash
# Find all the classes in /home/Public/workspace/CommonBirds/lib or somewhere else
provided=$1
if [ -z ${provided} ]; then
    lookhere=/home/Public/workspace/CommonBirds
else
    lookhere=${provided}
fi
find ${lookhere} -name '*.jar' -exec jar tf '{}' \; | tr / . | sed 's/\.class$//'
