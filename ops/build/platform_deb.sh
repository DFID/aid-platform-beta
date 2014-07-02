#!/bin/bash
set -xe

# Copy distribution zip and collate code required to build RPM.
mkdir -p deb/opt

unzip ./src/platform/dist/platform-1.0-SNAPSHOT.zip -d deb/opt
mv deb/opt/platform* deb/opt/platform

# Update permissions to allow the platform tool to start
chmod 755 deb/opt/platform/start
# Set version variable and create .deb
VERSION=$BUILD_NUMBER
fpm -s dir -t deb -a all -n platform -C deb -v $VERSION -p platform_$VERSION.deb .