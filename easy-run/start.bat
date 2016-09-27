@echo off

REM move to current directory. sources: http://stackoverflow.com/a/4420078/1483200 and Solr CMD script
set SDIR=%~dp0
IF "%SDIR:~-1%"=="\" set SDIR=%SDIR:~0,-1%
cd "%SDIR%"

REM setup environment
SET DBPTK=dbptk-app.jar
SET DBVTK_WORKSPACE=%SDIR%\dbvtk-data
SET JAVA_HOME=%SDIR%\jre\windows
SET CATALINA_HOME=%SDIR%\apache-tomcat

REM start
CALL solr\bin\solr.cmd start -c
CALL apache-tomcat\bin\startup.bat

REM provide instructions
echo The Database Visualization Toolkit will be available at: http://127.0.0.1:8080
echo
echo To add databases use the following command:
echo %JAVA_HOME%\bin\java -jar "-Dfile.encoding=UTF-8" "-Ddbvtk.workspace=%DBVTK_WORKSPACE%" "%SDIR%/%DBPTK%" -e solr -i siard-2 -if C:\path\to\siard_2\file

pause
