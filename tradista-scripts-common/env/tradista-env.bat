@echo off

set /p TRADISTA_VERSION=<"%~dp0/tradista-version.txt"

REM You can define here a Java home, it is optional
REM set "JAVA_HOME=path of your Java home"

if "%TRADISTA_VERSION%"=="" (
  echo ERROR: TRADISTA_VERSION is not defined
  exit /b 1
)
