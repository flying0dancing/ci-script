@echo off
REM ===========================================================================
REM             Contact Kun.Shen@lombardrisk.com if any bug    
REM ===========================================================================
rem set selfPath=%~dp0
rem set selfName=%~0
rem set logFullName=%selfPath%%selfName:.bat=.log%
set logFullName=%~dpn0.log

IF "%~1"=="" GOTO HELP
IF "%~1"=="/?" GOTO HELP
title package product...

if not "!PROCESSOR_ARCHITECTURE!"=="%PROCESSOR_ARCHITECTURE%" (
	cmd /V:ON /C %0 %*
    goto EOF
)

type nul>"%logFullName%"
call :LOGGER "%logFullName%" "========================= package log =========================================="

if not "%~4"=="" (
    if "%~5"=="" (
        GOTO MAIN
    ) else (
        call :LOGGER "%logFullName%" "[error] more arguments..."
        GOTO :HELP
    )
) else (
    call :LOGGER "%logFullName%" "[error] arguments missing..."
    GOTO :HELP
)

:MAIN
    set sourceFullPath=%~1
    set zipFullPath=%~2
    set properties=%~3
    pushd "%sourceFullPath%"    
    if "%errorlevel%"=="0" ( 
        if exist "%sourceFullPath%\dpm" (
            if exist "%sourceFullPath%\transforms" (
                if exist "%sourceFullPath%\forms" (
                    if exist "%sourceFullPath%\manifest.xml" (
                        GOTO PACKAGEED
                    ) else (
                        call :LOGGER "%logFullName%"  "[error]no such file:%sourceFullPath%\manifest.xml"
                        goto ERROREXIT
                    )
                ) else (
                    call :LOGGER "%logFullName%"  "[error]no such directory:%sourceFullPath%\forms"
                    goto ERROREXIT
                )
            ) else (
                call :LOGGER "%logFullName%"  "[error]no such directory:%sourceFullPath%\transforms"
                goto ERROREXIT
            )
        ) else (
            call :LOGGER "%logFullName%"  "[error]no such directory:%sourceFullPath%\dpm"
            goto ERROREXIT
        )
        
    ) else (
	    call :LOGGER "%logFullName%" "[error]no such directory:%sourceFullPath%"
	    goto ERROREXIT
    ) 
GOTO :EOF

:PACKAGEED
    for /f "eol=# tokens=2 delims==" %%i in ('findstr /i "gen.product.dpm.version" "%properties%"') do @set dpmVersion=%%i
    "%~dp0perl.exe" -i.bak -pwe "s/mappingVersion\=\".*\"/mappingVersion\=\"%dpmVersion%\"/g" manifest.xml 
    call :LOGGER "%logFullName%"  "[info] dpm mapping version is set to %dpmVersion% in manifest.xml"
    "%~dp0perl.exe" -i.bak -pwe "s/\<implementationVersion\>(\d+\.\d+\.\d+).*\<\/implementationVersion\>/\<implementationVersion\>$1-%~4\<\/implementationVersion\>/g" manifest.xml
    for /f %%i in ('%~dp0perl.exe -ne "print qq/$1/ if(/\<implementationVersion\>(.*)\<\/implementationVersion\>/);" manifest.xml') do set abcVersion=%%i
    call :LOGGER "%logFullName%"  "[info] implementationVersion is updated to %abcVersion% in manifest.xml"
    for /f "eol=# tokens=2 delims==" %%i in ('findstr /i "package.name.prefix" "%properties%"') do @set abcPrefix=%%i
    for /f "eol=# tokens=2 delims==" %%i in ('findstr /i "ar.installer.version" "%properties%"') do @set arVersion=%%i
    for /f "eol=# tokens=2 delims==" %%i in ('findstr /i "ocelot.config.sign.jar.windows" "%properties%"') do @set signJar=%%i
    set abcName=%abcPrefix%v%abcVersion%_for_AR_%arVersion%
    set zipFullName=%zipFullPath%\%abcName%.zip
    set lrmFullName=%zipFullPath%\%abcName%.lrm
    
    call :DelFile "%zipFullName%" "%logFullName%"
    call :DelFile "%lrmFullName%" "%logFullName%"
    call :LOGGER "%logFullName%"  "[info] package zip file [%zipFullName%]..."           
    "%~dp0zip.exe" -r "%zipFullName%" dpm/*.accdb transforms forms manifest.xml >>"%logFullName%"
    if exist "%zipFullName%" (
	    call :LOGGER "%logFullName%"  "[info] zip file is packaged."
        
        rem set signJar=!signJar:"=!
        if exist "%~dp0..\%signJar%" ( 
	        call :LOGGER "%logFullName%"  "[info] package lrm file [%lrmFullName%]..."
            java -jar "%~dp0..\%signJar%" "%zipFullName%"
            ren "%zipFullPath%\%abcName%_sign.lrm" "%abcName%.lrm"
            if exist "%lrmFullName%" (
                call :LOGGER "%logFullName%"  "[info] package is packaged successfully."
            ) else (
                call :LOGGER "%logFullName%"  "[error] cannot find packaged file [%lrmFullName%]"
                goto ERROREXIT
            )
        ) else (
            call :LOGGER "%logFullName%"  "[error] cannot find jar for sign [%~dp0..\%signJar%]"
            goto ERROREXIT
        )
    ) else (
        call :LOGGER "%logFullName%"  "[error] package failure."
        goto ERROREXIT
    )                    
GOTO :EOF

:LOGGER
    echo %~2
    echo %~2 >>"%~1"
GOTO :EOF

:DelFile
    if exist "%~1" ( 
	    call :LOGGER "%~2"  "[info] delete existed file:%~1"
        del /F /Q "%~1"
     ) 

GOTO :EOF
 
:HELP
ECHO ___________________________________________________________________
ECHO 			HELP DOCUMENT					
REM ECHO    
ECHO	Usage :: zipProduct [sourceFullPath] [zipFullPath] [propertiesFullName] [jenkinsVariable]
REM ECHO 
ECHO ___________________________________________________________________
goto :ERROREXIT

:ERROREXIT
 exit /b 1
goto :EOF

:EOF