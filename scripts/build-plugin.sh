#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
if command -v javac >/dev/null 2>&1; then
  exec "$ROOT/gradlew" --no-daemon clean test pluginManifest
fi
exec docker run --rm -v "$ROOT:/workspace" -w /workspace gradle:8.10.2-jdk17 gradle --no-daemon clean test pluginManifest
