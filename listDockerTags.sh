#!/bin/bash

IMAGE_NAME="f43nd1r/acrarium"
VERSION="$(git describe --abbrev=0)"

if [[ ${VERSION} == v* ]]; then
  VERSION=${VERSION#v}
fi

function tag {
  echo "${IMAGE_NAME}:$1"
  echo "ghcr.io/${IMAGE_NAME}:$1"
}

#tag "latest"
#if [[ $VERSION =~ ^[0-9]+(\.\d+)*$ ]]; then
#  tag "stable"
#fi
tag $VERSION
tag "next"
