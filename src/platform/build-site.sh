#!/bin/bash

# move into correct directory context
cd site

# remove any current build folder and recreate empty
rm -rf build && mkdir build

# move into build folder and clone current build repo
git clone git@github.com:DFID/aid-platform-site.git build/

rm -rf build/*

# generate site with middleman
bundle install
bundle exec middleman build --clean --verbose

# commit generated site
cd build
git add -A
git commit -m "Automated Commit through build-site.sh"
git push -f origin master
cd ..

# remove the unnecessary build directory
rm -rf build

