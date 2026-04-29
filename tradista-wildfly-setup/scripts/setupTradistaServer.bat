@echo off
setlocal enabledelayedexpansion

REM get JAVA_HOME, if configured
call "%~dp0\..\..\tradista-scripts-common\env\tradista-env.bat"

REM Check WILDFLY_HOME
if "%WILDFLY_HOME%"=="" (
  echo ERROR: WILDFLY_HOME is not set.
  exit /b 1
)

REM NOPAUSE variable is used in jboss-cli.bat, if it is set the script won't pause after commands execution
set "NOPAUSE=true"

xcopy %~dp0\..\modules %WILDFLY_HOME%\modules /E /I /Y
%WILDFLY_HOME%\bin\jboss-cli.bat --connect --file=%~dp0/commands.cli