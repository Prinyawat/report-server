#! /usr/bin/env bash
set -e

# THIS FILE BUILD LINUX IMAGE ONLY

REGISTRY="registry.gitlab.com/softsquare_ssru/registry"
IMAGE_NAME="reportsvr_cb"
TAG_DATETIME=$(date +%Y%m%d)
TAG_DATETIME_ISO=$(date +%Y-%m-%dT%H:%M)
COMMIT_SHORT_SHA="$(git rev-parse --short HEAD)"
CURRENT_REPO_BRANCH="$(git rev-parse --abbrev-ref HEAD)"
#build version branch_shortsha
BUILD_VERSION="$CURRENT_REPO_BRANCH_$COMMIT_SHORT_SHA"
MSYS_NO_PATHCONV=1

DOCKER_BUILDKIT=1 docker build --rm -f "docker/linux.Dockerfile" \
  --build-arg IMAGE_CREATED="${TAG_DATETIME_ISO}" \
  --build-arg IMAGE_VERSION="${BUILD_VERSION}" \
  --build-arg IMAGE_REVISION="${COMMIT_SHORT_SHA}" \
-t ${IMAGE_NAME} \
-t ${IMAGE_NAME}:${COMMIT_SHORT_SHA} \
-t ${IMAGE_NAME}:${TAG_DATETIME} \
-t ${REGISTRY}/${IMAGE_NAME} \
-t ${REGISTRY}/${IMAGE_NAME}:${COMMIT_SHORT_SHA} \
-t ${REGISTRY}/${IMAGE_NAME}:${TAG_DATETIME} .
