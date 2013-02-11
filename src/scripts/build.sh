#!/bin/bash

# To install Middleman on Ubuntu type:
# sudo apt-get install ruby-dev
# sudo gem install middleman
# 
# A JavaScript runtime must also be installed, to do so, type:
# sudo apt-get install nodejs

cd ../site
bundle exec middleman build --clean