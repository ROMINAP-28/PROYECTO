@echo off
setlocal
set "DIR=%~dp0"
if exist "%DIR%.maven\bin\mvn.cmd" (
    "%DIR%.maven\bin\mvn.cmd" %*
) else (
    echo [ERROR] Maven not found in .maven.
    exit /b 1
)
