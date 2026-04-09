@echo off
setlocal enabledelayedexpansion

REM get the Tradista version
call "%~dp0\..\..\..\..\..\tradista-scripts-common\env\tradista-env.bat"

REM client lib directory
set CLIENTLIBS_DIR="%~dp0\..\..\..\..\target\app-client-%TRADISTA_VERSION%-bin\app-client-%TRADISTA_VERSION%\lib"

REM determine java command
if defined JAVA_HOME (
    set JAVA_CMD="%JAVA_HOME%\bin\java"
) else (
    set JAVA_CMD=java
)

%JAVA_CMD% -Djava.naming.factory.initial=org.wildfly.naming.client.WildFlyInitialContextFactory -Djava.naming.provider.url=remote+http://localhost:8080  -Dprism.allowhidpi=false --add-modules=javafx.controls,javafx.media,javafx.fxml --module-path "%CLIENTLIBS_DIR%/javafx;%CLIENTLIBS_DIR%/javafx/win" -cp "%CLIENTLIBS_DIR%/*" org.eclipse.tradista.core.common.ui.view.MainEntry

echo Tradista Client started.