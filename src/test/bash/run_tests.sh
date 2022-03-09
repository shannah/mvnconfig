#!/bin/bash
set -e

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
source "$SCRIPTPATH/env.sh"
cd "$PROJECT_DIR"
mvn package
export TEST_DIR="$TARGET_DIR/tests/bash"
if [ -d "$TEST_DIR" ]; then
  rm -rf "$TEST_DIR"
fi

if [ ! -d "$TEST_DIR" ]; then
  mkdir -p "$TEST_DIR"
fi

for f in "$SCRIPTPATH/test_"*.sh; do
  echo "Running Test $f:"
  bash "$f"
done