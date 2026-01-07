#!/usr/bin/bash

# Resolve script directory
SCRIPT_HOME=$(dirname "$(readlink -f $0)")

export TRADISTA_VERSION="$(cat "$SCRIPT_HOME/tradista-version.txt")"

if [[ -z "$TRADISTA_VERSION" ]]; then
  echo "ERROR: TRADISTA_VERSION is not defined"
  exit 1
fi
