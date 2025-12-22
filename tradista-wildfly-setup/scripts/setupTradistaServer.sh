#!/bin/bash

# Check WILDFLY_HOME
if [ -z "$WILDFLY_HOME" ]; then
  echo "ERROR: WILDFLY_HOME is not set."
  exit 1
fi

SCRIPT_HOME=$(dirname "$(readlink -f $0)")

cp -r "$SCRIPT_HOME/../modules/org/"* "$WILDFLY_HOME/modules/org"
sh "$WILDFLY_HOME/bin/jboss-cli.sh" --connect --file="$SCRIPT_HOME/commands.cli"