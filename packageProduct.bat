@echo off
REM ===========================================================================
REM             Contact Kun.Shen@lombardrisk.com if any bug    
REM ===========================================================================

set _GEN_CONSTANT_SCRIPT_NAME="zipProduct.bat"
set _WorkSpace=%~dp0
rem echo %_WorkSpace%..\fed\hah.bat
rem %_WorkSpace%..\fed\hah.bat
rem set _WorkSpace=%_WorkSpace:ci-script\=%

set _GEN_VAR_SOURCE_FULLPATH=%_WorkSpace%..\%~1
set _GEN_VAR_ZIP_FULLPATH=%_WorkSpace%..\%~1
set _GEN_VAR_PROPERTY_FULLNAME=%_WorkSpace%..\%~2
set _GEN_VAR_JENKINS_VAR=%~3

"%~dp0%_GEN_CONSTANT_SCRIPT_NAME%" %_GEN_VAR_SOURCE_FULLPATH% %_GEN_VAR_ZIP_FULLPATH% %_GEN_VAR_PROPERTY_FULLNAME% %_GEN_VAR_JENKINS_VAR%

:EOF