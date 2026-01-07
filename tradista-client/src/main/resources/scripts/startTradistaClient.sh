#!/bin/bash

# Resolve script directory
SCRIPT_HOME=$(dirname "$(readlink -f $0)")

# Get the Tradista version
source "$SCRIPT_HOME/../../../../../tradista-scripts-common/env/tradista-env.sh"

# client lib directory
CLIENTLIBS_DIR="$SCRIPT_HOME/../../../../target/app-client-$TRADISTA_VERSION-bin/app-client-$TRADISTA_VERSION/lib"

java -Djava.naming.factory.initial=org.wildfly.naming.client.WildFlyInitialContextFactory -Djava.naming.provider.url=remote+http://localhost:8080 -Dprism.allowhidpi=false --add-modules=javafx.controls,javafx.media,javafx.fxml --module-path "$CLIENTLIBS_DIR/javafx:$CLIENTLIBS_DIR/javafx/linux" -cp "$CLIENTLIBS_DIR/*" org.eclipse.tradista.core.common.ui.view.MainEntry