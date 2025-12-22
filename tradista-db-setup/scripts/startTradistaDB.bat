@echo off
setlocal enabledelayedexpansion

REM Check DERBY_HOME
if "%DERBY_HOME%"=="" (
  echo ERROR: DERBY_HOME is not set.
  exit /b 1
)

echo Starting the database...
cd %DERBY_HOME%/bin
startNetworkServer.bat