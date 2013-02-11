#!/bin/bash

#
# Starts the project CMS that populates database and search engine
#

# Starts the search engine
sudo service elasticsearch start

# Starts the cms project
cd src/cms
../scripts/lib/sbt "project cms" run
cd ../..
