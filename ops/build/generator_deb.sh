#!/bin/bash
set -xe

mkdir -p deb/opt/generator
cp -R src/platform/site/* deb/opt/generator
cp src/platform/build-site.sh deb/opt/generator
chmod 755 deb/opt/generator/build-site.sh

# Set version variable and create .deb
VERSION=$BUILD_NUMBER
fpm -s dir -t deb -a all -n generator -C deb -v $VERSION -p generator_$VERSION.deb .