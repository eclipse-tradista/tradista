@echo off
setlocal enabledelayedexpansion

REM Check WILDFLY_HOME
if "%WILDFLY_HOME%"=="" (
  echo ERROR: WILDFLY_HOME is not set.
  exit /b 1
)

xcopy %~dp0\..\modules\org %WILDFLY_HOME%\modules\org /E /I /Y
%WILDFLY_HOME%\bin\jboss-cli.bat --connect --file=%~dp0/commands.cli