#!/bin/bash

# Check WILDFLY_HOME
if [ -z "$WILDFLY_HOME" ]; then
  echo "ERROR: WILDFLY_HOME is not set."
  exit 1
fi

TRADISTA_VERSION=3.1.0-RC1
echo "Tradista version: $TRADISTA_VERSION"

# Resolve script directory
SCRIPT_HOME=$(dirname "$(readlink -f $0)")

DEPLOYMENTS_DIR="$WILDFLY_HOME/standalone/deployments"

# List of applications: folder|artifact
ALL_EARS_OK=true

APPS=(
  "tradista-app|app"
  "tradista-transfer-app|transfer-app"
  "tradista-position-app|position-app"
  "tradista-marketdata-app|marketdata-app"
  "tradista-message-app|message-app"
  "tradista-importer-app|importer-app"
)

# Check phase
for APP in "${APPS[@]}"; do
  IFS="|" read -r FOLDER ARTIFACT <<< "$APP"

  EAR_PATH="$SCRIPT_HOME/../../$FOLDER/target/$ARTIFACT-$TRADISTA_VERSION.ear"

  if [ ! -f "$EAR_PATH" ]; then
    echo "ERROR: Missing EAR: $EAR_PATH"
    ALL_EARS_OK=false
  fi
done

if [ "$ALL_EARS_OK" = false ]; then
  echo "Aborting deployment due to missing EAR files."
  exit 1
fi

echo "All EAR files found."

# Cleanup phase (FULL WIPE)
echo "Cleaning deployment directory: $DEPLOYMENTS_DIR"
rm -rf "$DEPLOYMENTS_DIR"/*

echo "Cleanup completed. Deploying..."

for APP in "${APPS[@]}"; do
  IFS="|" read -r FOLDER ARTIFACT <<< "$APP"

  EAR_PATH="$SCRIPT_HOME/../../$FOLDER/target/$ARTIFACT-$TRADISTA_VERSION.ear"

  cp "$EAR_PATH" "$DEPLOYMENTS_DIR/"
done

echo "Deployment completed successfully."