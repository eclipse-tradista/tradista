#!/bin/bash

# Check WILDFLY_HOME
if [ -z "$WILDFLY_HOME" ]; then
  echo "ERROR: WILDFLY_HOME is not set."
  exit 1
fi

sh "$WILDFLY_HOME/bin/standalone.sh" -b localhost --server-config=standalone-full.xml