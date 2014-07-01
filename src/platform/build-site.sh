#!/bin/bash

# move into correct directory context
cd /opt/generator
source /etc/profile.d/rbenv.sh

# Set ENV['HOME']
export HOME=/root

# remove backup web directory
rm -Rf /var/www-bak

# remove any current build folder and recreate empty
rm -Rf /opt/generator/build && mkdir /opt/generator/build

# generate site with middleman
rbenv exec bundle install
rbenv exec bundle exec 'middleman build --clean --verbose'
cp /var/www /var/www-bak
find /var/www -mindepth 1 -delete
cp -fR /opt/generator/build/* /var/www