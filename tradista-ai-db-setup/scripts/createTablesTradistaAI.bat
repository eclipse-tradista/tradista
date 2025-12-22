@echo off
setlocal enabledelayedexpansion

REM Check DERBY_HOME
if "%DERBY_HOME%"=="" (
  echo ERROR: DERBY_HOME is not set.
  exit /b 1
)

echo Executing create commands...
%DERBY_HOME%/bin/ij %~dp0/createCommands.txt