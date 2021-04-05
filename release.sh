#!/bin/bash

echo "Type version to release [Current version: $(./mvnw org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep '^[0-9]')]"
read -r version
git tag -a "v$version" -m "Create version $version"
git push --tags