#!/bin/sh
DIR="$(cd "$(dirname "$0")" && pwd)"
if [ -f "$DIR/.maven/bin/mvn" ]; then
    exec "$DIR/.maven/bin/mvn" "$@"
else
    echo "[ERROR] Maven not found in .maven."
    exit 1
fi
