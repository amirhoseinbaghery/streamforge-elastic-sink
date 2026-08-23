#!/bin/sh
set -eu
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=8.10.2
DIST_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
ZIP="$DIST_DIR/gradle-${GRADLE_VERSION}-bin.zip"
INSTALL="$DIST_DIR/gradle-${GRADLE_VERSION}"
if [ ! -x "$INSTALL/bin/gradle" ]; then
  mkdir -p "$DIST_DIR"
  if [ ! -f "$ZIP" ]; then
    curl --fail --location --retry 3 --output "$ZIP" "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
  fi
  tmp="$DIST_DIR/.extract.$$"
  mkdir -p "$tmp"
  unzip -q "$ZIP" -d "$tmp"
  mv "$tmp/gradle-${GRADLE_VERSION}" "$INSTALL"
  rmdir "$tmp"
fi
exec "$INSTALL/bin/gradle" "$@"
