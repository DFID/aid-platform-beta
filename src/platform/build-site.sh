#!/bin/bash

cd site
rm -rf build && mkdir build
bundle install
bundle exec middleman build --clean