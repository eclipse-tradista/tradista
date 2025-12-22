@echo off
setlocal enabledelayedexpansion

REM Check WILDFLY_HOME
if "%WILDFLY_HOME%"=="" (
  echo ERROR: WILDFLY_HOME is not set.
  exit /b 1
)

REM Target deployment directory
set DEPLOYMENTS_DIR=%WILDFLY_HOME%\standalone\deployments

REM Cleanup phase (FULL WIPE)
echo Cleaning deployment directory: %DEPLOYMENTS_DIR%
del /q "%DEPLOYMENTS_DIR%\*"

echo Cleanup completed.