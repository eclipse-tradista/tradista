#!/bin/bash

# Check DERBY_HOME
if [ -z "$DERBY_HOME" ]; then
  echo "ERROR: DERBY_HOME is not set."
  exit 1
fi

SCRIPT_HOME=$(dirname "$(readlink -f $0)")

echo "Executing drop commands..."
sh "$DERBY_HOME/bin/ij" "$SCRIPT_HOME/dropCommands.txt"