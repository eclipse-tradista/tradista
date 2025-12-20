#!/bin/bash

# Check DERBY_HOME
if [ -z "$DERBY_HOME" ]; then
  echo "ERROR: DERBY_HOME is not set."
  exit 1
fi

sh "$DERBY_HOME/bin/startNetworkServer"