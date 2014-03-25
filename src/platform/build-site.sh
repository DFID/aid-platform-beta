#!/bin/bash

# move into correct directory context
cd /opt/generator

# remove backup web directory
rm -Rf /var/www-bak

# remove any current build folder and recreate empty
rm -Rf build && mkdir build

# generate site with middleman
bundle install
bundle exec 'middleman build --clean --verbose'
cp -R /var/www /var/www-bak
find /var/www -mindepth 1 -delete
cp -fR ./build/* /var/www