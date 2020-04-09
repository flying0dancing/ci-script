@echo OFF

set filename=%~1
set srcfolder=%~2
set resultfolder=%~3
echo Begin automation testing 
echo -DxmlFileName=%filename% -DsrcFolder=%srcfolder% -DresultFolder=%resultfolder%
Pause
