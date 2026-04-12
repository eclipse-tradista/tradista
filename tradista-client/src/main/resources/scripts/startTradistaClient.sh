#!/bin/bash

# Resolve script directory
SCRIPT_HOME=$(dirname "$(readlink -f $0)")

# Get the Tradista version
source "$SCRIPT_HOME/../../../../../tradista-scripts-common/env/tradista-env.sh"

# client lib directory
CLIENTLIBS_DIR="$SCRIPT_HOME/../../../../target/app-client-$TRADISTA_VERSION-bin/app-client-$TRADISTA_VERSION/lib"

# determine java command
if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD="java"
fi

$JAVA_CMD -Djava.naming.factory.initial=org.wildfly.naming.client.WildFlyInitialContextFactory -Djava.naming.provider.url=remote+http://localhost:8080 -Dprism.allowhidpi=false --add-modules=javafx.controls,javafx.media,javafx.fxml --module-path "$CLIENTLIBS_DIR/javafx:$CLIENTLIBS_DIR/javafx/linux" -cp "$CLIENTLIBS_DIR/*" org.eclipse.tradista.core.common.ui.view.MainEntry

echo "Tradista Client started."