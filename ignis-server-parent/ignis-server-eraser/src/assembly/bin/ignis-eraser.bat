@echo off
setlocal

SET DIR=%~dp0

set "currentLocation=%~dp0"
echo currentLocation:              "%currentLocation%"

call :GetDirParentN root "%currentLocation%" ".."
echo root:            	"%root%"

call :RunEraser

:GetFileBaseDir
    :: sets the value to dirFileBase variable
    set "%~1=%~dp2"
    exit /b 0

:GetDirParentN
    for %%I in ("%~2\%~3") do set "%~1=%%~fI"
    exit /b 0


:RunEraser
  FOR /F "tokens=* USEBACKQ" %%F IN (`where /r %root%\lib ignis-server-eraser-exec.jar`) DO (
  SET JAR_FILE=%%F
  )
  ECHO %JAR_FILE%

  SET PROPERTIES=%root%\conf
  SET JAVA_OPTS=-Xmx512m --spring.config.location=%PROPERTIES%\eraser.properties

  SET ignisEraserScript=java -jar %JAR_FILE% %JAVA_OPTS%

  echo "Starting Ignis Eraser... %ignisEraserScript%"
  %ignisEraserScript%

