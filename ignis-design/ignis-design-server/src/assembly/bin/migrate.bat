@echo off
setlocal

SET DIR=%~dp0

set "currentLocation=%~dp0"
echo currentLocation:              "%currentLocation%"

call :GetDirParentN root "%currentLocation%" ".."
echo root:            	"%root%"


SET FLYWAY_HOME="%root%\lib\flyway-5.0.7"
SET LOG="%root%\logs\migrate.log"

SET migrateScript=%FLYWAY_HOME%\flyway migrate

echo "Migrating design studio db... %migrateScript%"
%migrateScript%


:GetFileBaseDir
    :: sets the value to dirFileBase variable
    set "%~1=%~dp2"
    exit /b 0
	
:GetDirParentN
    for %%I in ("%~2\%~3") do set "%~1=%%~fI"
    exit /b 0
	
