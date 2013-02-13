#!/bin/bash

#
# Starts the project CMS that populates database and search engine
#

# Update elasticsearch data directory

# Starts the cms project
cd src/cms
play clean dist
cd ../..
