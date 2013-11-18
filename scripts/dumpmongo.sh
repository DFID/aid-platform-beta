#!/bin/bash

cd ./build
git pull origin master
cd ..

mkdir -p ./build/data/mongo

mongodump --db dfid --out ./build/data/mongo

# Commit new data
cd build
git add -A
git commit -m "Automated Commit: Mongo Data Export"
git push origin master
cd ..