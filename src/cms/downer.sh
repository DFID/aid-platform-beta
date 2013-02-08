#!/bin/bash

CURRENT=$(pwd)

for VERSION in "1.0" "1.01" "1.02"
do
  for FILE in "iati-activities-schema.xsd" "iati-organisations-schema.xsd" "iati-registry-record-schema.xsd" "iati-common.xsd" "xml.xsd"
  do
  	TARGET_PATH="modules/validator/src/main/resources/xsd/"$VERSION
    URL="http://iatistandard.org/downloads/"$VERSION"/"$FILE

    mkdir -p $TARGET_PATH ; cd $TARGET_PATH

    echo $URL
    curl -O --progress-bar $URL
    cd $CURRENT
  done
done