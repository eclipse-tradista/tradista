@echo off
setlocal enabledelayedexpansion

REM Check DERBY_HOME
if "%DERBY_HOME%"=="" (
  echo ERROR: DERBY_HOME is not set.
  exit /b 1
)

echo Stopping the database...
%DERBY_HOME%/bin/stopNetworkServer.bat