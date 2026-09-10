#!/bin/bash
# Script to build the ukge python wheel file inside dist folder
pushd /nfs/workspace/CommonBirds/local/python/reuse
rm -fR build dist # Get rid of older versions
python3 -m build
echo "To install try:"
echo "pip install $(pwd)/dist/ukge*.whl"
