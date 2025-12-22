#!/bin/bash

# Check WILDFLY_HOME
if [ -z "$WILDFLY_HOME" ]; then
  echo "ERROR: WILDFLY_HOME is not set."
  exit 1
fi

DEPLOYMENTS_DIR="$WILDFLY_HOME/standalone/deployments"

# Cleanup phase (FULL WIPE)
echo "Cleaning deployment directory: $DEPLOYMENTS_DIR"
rm -rf "$DEPLOYMENTS_DIR"/*

echo "Cleanup completed."