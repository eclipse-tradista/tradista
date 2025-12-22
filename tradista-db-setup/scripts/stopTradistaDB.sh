#!/bin/bash

# Check DERBY_HOME
if [ -z "$DERBY_HOME" ]; then
  echo "ERROR: DERBY_HOME is not set."
  exit 1
fi

echo Stopping the database...
sh "$DERBY_HOME/bin/stopNetworkServer"