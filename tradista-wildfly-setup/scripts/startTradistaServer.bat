@echo off
setlocal enabledelayedexpansion

REM Check WILDFLY_HOME
if "%WILDFLY%"=="" (
  echo ERROR: WILDFLY_HOME is not set.
  exit /b 1
)

%WILDFLY_HOME%/bin/standalone.bat -b localhost --server-config=standalone-full.xml