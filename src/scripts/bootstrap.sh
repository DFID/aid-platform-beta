#!/bin/bash

# checkout latest sources from repository
git pull origin master

# start cms database

# load IATI data
./src/scripts/load.sh

# starting the api application
cd src/api/
play clean compile stage
cd ../..

# build and start the site application
./src/scripts/build.sh
bundle exec middleman server
