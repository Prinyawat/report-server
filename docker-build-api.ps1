$REGISTRY = "registry.gitlab.com/softsquare_ssru/registry"
$IMAGE_NAME = "reportsvr_cb_win"
$COMMON_IMAGE_NAME = "reportsvr_cb"
$TAG_DATETIME = (get-date).toString('yyyMMdd')
$COMMIT_SHORT_SHA = (git rev-parse --short HEAD)
$TAG_DATETIME_ISO = (get-date).toString('yyy-MM-ddThh:mm')
$CURRENT_REPO_BRANCH = (git rev-parse --abbrev-ref HEAD)
#build version branch_shortsha
$BUILD_VERSION = "$CURRENT_REPO_BRANCH_$COMMIT_SHORT_SHA"
#$MSYS_NO_PATHCONV = 1

docker build . --rm -f "docker/Dockerfile" `
  --build-arg IMAGE_CREATED="${TAG_DATETIME_ISO}" `
  --build-arg IMAGE_VERSION="${BUILD_VERSION}" `
  --build-arg IMAGE_REVISION="${COMMIT_SHORT_SHA}" `
  -t ${IMAGE_NAME} `
  -t ${IMAGE_NAME}:${COMMIT_SHORT_SHA} `
  -t ${IMAGE_NAME}:${TAG_DATETIME} `
  -t ${REGISTRY}/${IMAGE_NAME} `
  -t ${REGISTRY}/${IMAGE_NAME}:${COMMIT_SHORT_SHA} `
  -t ${REGISTRY}/${IMAGE_NAME}:${TAG_DATETIME}

Write-Host "You can use command below to push image"
Write-Host  "  docker push -q ${REGISTRY}/${IMAGE_NAME}:${TAG_DATETIME}"
Write-Host  "  docker push -q ${REGISTRY}/${IMAGE_NAME}:${COMMIT_SHORT_SHA}"
Write-Host  "  docker push -q ${REGISTRY}/${IMAGE_NAME}"