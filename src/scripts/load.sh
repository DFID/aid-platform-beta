#!/bin/bash

#
# Starts the project CMS that populates database and search engine
#

# Starts the search engine
sudo service elasticsearch start

# Starts the cms project
cd src/cms
play clean compile stage
cd ../..
