@echo off
setlocal enabledelayedexpansion

REM Check WILDFLY_HOME
if "%WILDFLY_HOME%"=="" (
  echo ERROR: WILDFLY_HOME is not set.
  exit /b 1
)

set TRADISTA_VERSION=3.0.0

echo Tradista version: %TRADISTA_VERSION%

REM Target deployment directory
set DEPLOYMENTS_DIR=%WILDFLY_HOME%\standalone\deployments

REM List of applications: folder|artifact
set ALL_EARS_OK=true

for %%A in (
  "tradista-app|app"
  "tradista-transfer-app|transfer-app"
  "tradista-position-app|position-app"
  "tradista-marketdata-app|marketdata-app"
  "tradista-message-app|message-app"
  "tradista-importer-app|importer-app"
) do (
  for /f "tokens=1,2 delims=|" %%B in (%%A) do (
    set EAR_PATH=%~dp0\..\..\%%B\target\%%C-%TRADISTA_VERSION%.ear

    if not exist "!EAR_PATH!" (
      echo ERROR: Missing EAR: !EAR_PATH!
      set ALL_EARS_OK=false
    )
  )
)

if "!ALL_EARS_OK!"=="false" (
  echo Aborting deployment due to missing EAR files.
  exit /b 1
)

echo All EAR files found.

REM Cleanup phase (FULL WIPE)
echo Cleaning deployment directory: %DEPLOYMENTS_DIR%
del /q "%DEPLOYMENTS_DIR%\*"

echo Cleanup completed. Deploying...

REM Copy phase
for %%A in (
  "tradista-app|app"
  "tradista-transfer-app|transfer-app"
  "tradista-position-app|position-app"
  "tradista-marketdata-app|marketdata-app"
  "tradista-message-app|message-app"
  "tradista-importer-app|importer-app"
) do (
  for /f "tokens=1,2 delims=|" %%B in (%%A) do (
    set EAR_PATH=%~dp0\..\..\%%B\target\%%C-!TRADISTA_VERSION!.ear

    xcopy "!EAR_PATH!" %WILDFLY_HOME%\standalone\deployments /Y >nul
  )
)

echo Deployment completed successfully.