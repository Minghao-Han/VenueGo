#!/bin/bash
# Shared build script used by per-service build.sh wrappers and the Makefile.
# Usage: build-service.sh <ServiceDir> <image-name> <tag>
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REGISTRY="${REGISTRY:-localhost:32770}"

SERVICE_DIR=$1
IMAGE_NAME=$2
TAG=${3:-$(git -C "$ROOT" rev-parse --short HEAD)}

if [[ -z "$SERVICE_DIR" || -z "$IMAGE_NAME" ]]; then
  echo "Usage: $0 <ServiceDir> <image-name> [tag]" >&2
  exit 1
fi

echo "--- Building $IMAGE_NAME:$TAG ---"
cd "$ROOT/$SERVICE_DIR"
docker build -t "$IMAGE_NAME:$TAG" .
docker push "$IMAGE_NAME:$TAG"
echo "✅ Pushed: $IMAGE_NAME:$TAG"
echo "$TAG" > "$ROOT/.last-build-tag"
