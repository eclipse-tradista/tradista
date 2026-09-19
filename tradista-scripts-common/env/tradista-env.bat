@echo off

set /p TRADISTA_VERSION=<"%~dp0/tradista-version.txt"

REM You can define here a Java home, it is optional
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-24.0.2.12-hotspot"

if "%TRADISTA_VERSION%"=="" (
  echo ERROR: TRADISTA_VERSION is not defined
  exit /b 1
)
