#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
: "${BASE_IMAGE:?Set BASE_IMAGE to the reviewed Kafka Connect 4.0-compatible runtime image}"
: "${IMAGE_REF:?Set IMAGE_REF to a versioned local image name}"
"$ROOT/scripts/build-plugin.sh"
docker build --pull=false --build-arg BASE_IMAGE="$BASE_IMAGE" -f "$ROOT/docker/Dockerfile" -t "$IMAGE_REF" "$ROOT"
docker image inspect "$IMAGE_REF" --format 'IMAGE_ID={{.Id}}'
