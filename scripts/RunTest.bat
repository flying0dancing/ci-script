@echo OFF
set filename=%~1
set srcfolder=%~2
set resultfolder=%~3

for /f "tokens=2 delims=:" %%a in ('ipconfig ^|find "IPv4 Address"') do for /f "tokens=4 delims=." %%b in ("%%a") do (set "machine=%%b")
set regulator=%~4
@echo %regulator%
rem curl -XDELETE http://172.20.30.89:9200/log%machine%-%regulator%-*

@echo Begin automation testing
@echo mvn test -DxmlFileName=%filename% -DsrcFolder=%srcfolder% -DresultFolder=%resultfolder% -Dgo
