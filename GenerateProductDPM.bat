@echo off
REM ===========================================================================
REM             Contact Kun.Shen@lombardrisk.com if any bug    
REM ===========================================================================

set _GEN_CONSTANT_SCRIPT_NAME="GenProductDPM.vbs"
set _WorkSpace=%~dp0
rem set _WorkSpace=%_WorkSpace:ci-script\=%
REM =============== need to modify vars ======================
REM =============== start ======================

set _GEN_VAR_SCHEMA_FULLNAME=%_WorkSpace%..\%~1
set _GEN_VAR_DB_FULLNAME=%_WorkSpace%..\%~2
set _GEN_VAR_DATA_PATH=%_WorkSpace%..\%~3
set _GEN_VAR_LOG_FULLNAME="%~dp0log\%~n0.log"

REM =============== end ======================
REM =============== need to modify vars ======================

if /I "%PROCESSOR_ARCHITECTURE%"=="x86" (
	echo run on x86
	echo.
    cmd /k cscript //nologo "%~dp0%_GEN_CONSTANT_SCRIPT_NAME%" %_GEN_VAR_SCHEMA_FULLNAME% %_GEN_VAR_DB_FULLNAME% %_GEN_VAR_DATA_PATH% %_GEN_VAR_LOG_FULLNAME%
	
) ELSE (
    echo run on wow64
	echo.
   %SystemRoot%\SysWow64\cmd.exe /k cscript //nologo "%~dp0%_GEN_CONSTANT_SCRIPT_NAME%" %_GEN_VAR_SCHEMA_FULLNAME% %_GEN_VAR_DB_FULLNAME% %_GEN_VAR_DATA_PATH% %_GEN_VAR_LOG_FULLNAME%
)


:EOF