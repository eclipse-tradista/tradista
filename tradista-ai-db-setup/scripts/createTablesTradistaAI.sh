#!/bin/bash

# Check DERBY_HOME
if [ -z "$DERBY_HOME" ]; then
  echo "ERROR: DERBY_HOME is not set."
  exit 1
fi

SCRIPT_HOME=$(dirname "$(readlink -f $0)")

echo "Executing create commands..."
sh "$DERBY_HOME/bin/ij" "$SCRIPT_HOME/createCommands.txt"