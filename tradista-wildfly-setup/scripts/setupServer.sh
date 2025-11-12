#!/bin/bash
cp -r ../module/org/* "$WILDFLY_HOME/modules/org"
$WILDFLY_HOME\bin\jboss-cli.sh --connect --file=./commands.cli