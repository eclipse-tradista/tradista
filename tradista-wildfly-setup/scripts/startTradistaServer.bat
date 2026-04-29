@echo off
setlocal enabledelayedexpansion

REM get JAVA_HOME, if configured
call "%~dp0\..\..\tradista-scripts-common\env\tradista-env.bat"

REM Check WILDFLY_HOME
if "%WILDFLY_HOME%"=="" (
  echo ERROR: WILDFLY_HOME is not set.
  exit /b 1
)

%WILDFLY_HOME%/bin/standalone.bat -b localhost --server-config=standalone-full.xml