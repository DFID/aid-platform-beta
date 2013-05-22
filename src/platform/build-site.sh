#!/bin/bash

# move into correct directory context
cd site

# remove any current build folder and recreate empty
rm -rf build && mkdir build

# TODO 2013-05-14 James Hughes - Removed this as we are operating under in standalone mode
# move into build folder and clone current build repo
# git clone git@github.com:DFID/aid-platform-site.git build/
# rm -rf build/*

# generate site with middleman
bundle install
bundle exec 'middleman build --clean --verbose'

# if the DFID_STATIC_FILE_PATH variable is set then we can
# deploy the site locally
if [ "$DFID_STATIC_FILE_PATH" != "" ]; then
    find $DFID_STATIC_FILE_PATH -mindepth 1 -delete
    cp -fR ./build/* $DFID_STATIC_FILE_PATH
fi

# TODO 2013-05-14 James Hughes - Removed this as we are operating under in standalone mode
# commit generated site
# cd build
# git add -A
# git commit -m "Automated Commit through build-site.sh"
# git push -f origin master
# cd ..

# remove the unnecessary build directory
rm -rf build