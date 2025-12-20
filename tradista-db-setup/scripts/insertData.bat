@echo off
setlocal enabledelayedexpansion

REM Check DERBY_HOME
if "%DERBY_HOME%"=="" (
  echo ERROR: DERBY_HOME is not set.
  exit /b 1
)

echo Executing insertion commands...
%DERBY_HOME%/bin/ij %~dp0/insertData.txt
