#!/bin/bash

echo "Type version to release [Current version: $(git describe --abbrev=0 | sed -e "s/^v//")]"
read -r version
git tag -a "v$version" -m "Create version $version"
git push --tags