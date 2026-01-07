@echo off

set /p TRADISTA_VERSION=<"%~dp0/tradista-version.txt"

if "%TRADISTA_VERSION%"=="" (
  echo ERROR: TRADISTA_VERSION is not defined
  exit /b 1
)
