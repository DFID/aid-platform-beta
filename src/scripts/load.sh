#!/bin/bash

#
# Starts the project CMS that populates database and search engine
#

# Starts the search engine
sudo service elasticsearch start

# Starts the cms project
cd ../cms
../scripts/lib/sbt "project cms" run
