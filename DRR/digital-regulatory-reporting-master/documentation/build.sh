#!/usr/bin/env bash

################################################################################
################################################################################
###### Useful script to test the doc creation process *locally*
###### Branch is specified as a parameter (or master if none passed)
######
###### Assumed installations:
######  1) docker (must also be up and running)
######  2) jq
################################################################################
################################################################################

red=$(tput setaf 1)
azul=$(tput setaf 44)
reset=$(tput sgr0)

## make sure it is run with proper directory
bin=$(dirname "$0")
bin=$(cd "$bin"; pwd)

printf "Copying DRR artefacts... \n"

## Make a build dir and copy everythign that needs to built
rm -rf build
mkdir build
mkdir build/source

## root files
cp *.rst build

## cdm
cp -r source/* build/source
export LATEST_RELEASE_ID=$1
echo "... LATEST_RELEASE_ID is $LATEST_RELEASE_ID"
envsubst \$LATEST_RELEASE_ID < source/links-template.rst >  build/source/links.rst
## sphynx
cp -r site/* build
