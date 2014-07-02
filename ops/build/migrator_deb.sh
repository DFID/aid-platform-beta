#!/bin/bash
set -xe

mkdir -p deb/opt/migrator/

cp -R src/migrator/* deb/opt/migrator/

# Set version variable and create .deb
VERSION=$BUILD_NUMBER
fpm -s dir -t deb -a all -n migrator -C deb -v $VERSION -p migrator_$VERSION.deb .