#!/bin/bash

# check all the commands exist and bail early
for cmd in 'curl' 'sed' 'jq' 'wget'
do
  command -v $cmd >/dev/null 2>&1 || { echo >&2 "I require "$cmd" but it's not installed.  Aborting."; exit 1; }
done

mkdir -p sources/xml/organisations sources/xml/activities

echo 'Retrieving list of organisation sources from IATI Registry'
curl --silent 'http://www.iatiregistry.org/api/search/dataset?filetype=organisation&all_fields=1&limit=4000&publisher_iati_id=GB-1' | jq '.results[] | .download_url' | sed 's/^["]*//;s/["]*$//' > sources/organisations

echo 'Retrieving list of activity sources from IATI Registry'
curl --silent 'http://www.iatiregistry.org/api/search/dataset?filetype=activity&all_fields=1&limit=4000&publisher_iati_id=GB-1' | jq '.results[] | .download_url' | sed 's/^["]*//;s/["]*$//' > sources/activities

cd sources/xml/organisations
echo 'Downloading organisation files'
wget --quiet -i ../../organisations

cd ../activities
echo 'Downloading activity files'
wget --quiet -i ../../activities

cd ..

echo 'Adding .xml extension due to clobbering of files'
for file in activities/*
do
  mv "$file" "$file.xml"
done

for file in organisations/*
do
  mv "$file" "$file.xml"
done
