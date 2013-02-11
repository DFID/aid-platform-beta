#!/bin/bash

# checkout latest sources from repository
git pull origin master

# start cms database

# load IATI data
./load.sh

# starting the api application
cd ../api/
../scripts/lib/sbt "project api" run

# build and start the site application
./build.sh
bundle exec middleman server
