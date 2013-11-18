#!/bin/bash

cd ./build
git pull origin master
cd ..

mongorestore --db dfid "./build/data/mongo/dfid"
