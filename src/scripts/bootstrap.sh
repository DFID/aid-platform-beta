#!/bin/bash

echo 'Cleaning the previous build directory...'
rm -rf build
mkdir build

echo 'Fetching latest revision from github repository...'
git pull origin master

# start cms database

echo 'Loading IATI data...'
./src/scripts/load.sh

echo 'Copying cms application to common build folder...'
mkdir build/cms
cp src/cms/dist/cms* build/cms/

echo 'Building the api application...'
cd src/api/
play clean dist
cd ../..

echo 'Copying api application to common build folder...'
mkdir build/api
cp src/api/dist/api* build/api/

echo 'Building the site application...'
./src/scripts/build.sh

echo 'Copying site application to common build folder...'
mkdir build/site
cp -r src/site/build/* build/site/
