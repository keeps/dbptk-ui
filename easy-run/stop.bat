@echo off

REM move to current directory. sources: http://stackoverflow.com/a/4420078/1483200 and Solr CMD script
set SDIR=%~dp0
IF "%SDIR:~-1%"=="\" set SDIR=%SDIR:~0,-1%
cd "%SDIR%"

REM setup environment
SET DBPTK=dbptk-app.jar
SET DBVTK_HOME=%SDIR%\dbvtk-data
SET JAVA_HOME=%SDIR%\jre\windows
SET CATALINA_HOME=%SDIR%\apache-tomcat

REM shutdown
echo "Shutting down Solr server"
CALL solr\bin\solr.cmd stop -all

echo "Shutting down tomcat server"
CALL apache-tomcat\bin\shutdown.bat

REM clean
echo "Cleaning up"
rd /s /q log
del /f dbptk-app-*.log.txt
del /f dbptk-report-*.txt

echo "Done."

pause
