@echo OFF

IF "%~1"=="" GOTO Help
IF "%~1"=="/?" GOTO Help

set destPath=%~1
set resultFolder=%~2
set today=%~3


set dest=%destPath%%resultFolder%
IF EXIST "%dest%" (
    set dest=%destPath%%resultFolder%%today%
)
set acc=1
:getNewName
IF EXIST "%dest%" (
    set /a acc=%acc%+1
    set dest=%destPath%%resultFolder%%today%%acc%
    goto getNewName
) ELSE (
    @echo %dest%
)
goto END

:Help
ECHO ___________________________________________________________________
ECHO 			HELP DOCUMENT					
REM ECHO    
ECHO	Usage :: %~nx0 destParentPath destFolderName newFolderNameSuffix
REM ECHO 
ECHO	destParentPath :: destFolderName's parent Directory (drive:\path\ or \\server\share\path\)
ECHO	destFolderName :: folder under destParentPath
ECHO	newFolderNameSuffix :: [optional] like date YYYYMMDD
REM ECHO 
ECHO ___________________________________________________________________
GOTO END



:END