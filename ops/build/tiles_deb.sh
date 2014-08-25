#!/bin/bash
set -xe

mkdir -p deb/opt/tiles
cp src/tilestream/* deb/opt/tiles/
# Set version variable and create .deb
VERSION=$BUILD_NUMBER
fpm -s dir -t deb -a all -n tiles -C deb -v $VERSION -p tiles_$VERSION.deb .