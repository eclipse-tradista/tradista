#!/bin/bash

java -Djava.naming.factory.initial=org.wildfly.naming.client.WildFlyInitialContextFactory -Djava.naming.provider.url=remote+http://localhost:8080 -Dprism.allowhidpi=false --add-modules=javafx.controls,javafx.media,javafx.fxml --module-path ./lib/javafx -cp "./lib/*" org.eclipse.tradista.core.common.ui.view.MainEntry