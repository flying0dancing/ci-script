@echo OFF

for /f "tokens=2 delims=:" %%a in ('ipconfig ^|find "IPv4 Address"') do for /f "tokens=4 delims=." %%b in ("%%a") do (set "machine=%%b")
set regulator=hongkongmonetaryauthority

:_reImp
set /p reImport=Do you want to clear up existing automation dashbaord data (Y/N)?

if  "%reImport%" =="Y" (
 curl -XDELETE http://172.20.30.89:9200/log%machine%-%regulator%-*
 echo going to remove all existing dashbaord log data
) else if "%reImport%" =="N" (
 echo keeping all existing dashbaord log data
) else (
 echo input value illegal, please re-input...
        goto _reImp  
)

set filename=%~1
set srcfolder=%~2
set resultfolder=%~3

echo Begin automation testing

mvn test -DxmlFileName=%filename% -DsrcFolder=%srcfolder% -DresultFolder=%resultfolder% -Dgo& Pause
