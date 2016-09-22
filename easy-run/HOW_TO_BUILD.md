NOTE: These are technical instructions to build the distribution package.
      For instructions on how to run the Database Visualization Toolkit, see the README.md file in this folder.

# Setup steps

1. In DBPTK source folder, run `mvn install -Pcommon`
2. In DBVTK source folder, run `mvn install -Pcommon`
3. In DBPTK source folder, run `mvn package`
4. In DBVTK source folder, run `mvn package`
5. create a folder where the distribution package will be assembled, henceforth called the destination folder
6. copy everything in DBVTK `easy-run` folder to the destination folder
7. download appropriate solr version from [here](http://lucene.apache.org/solr/downloads.html) and extract it in the destination folder to a subfolder called solr
8. download tomcat8 zip from [here](https://tomcat.apache.org/download-80.cgi) and extract it to a folder named `apache-tomcat`
9. copy dbvtk-model/src/main/resources/solr-configset to the destination folder
10. copy dbptk-core/target/dbptk-app-X.Y.Z.jar to the destination folder and rename it to dbptk-app.jar
11. delete the contents of (destination folder)/apache-tomcat/webapps/ folder
12. create destination subfolder apache-tomcat/webapps/ROOT/
13. extract dbvtk-viewer/target/dbvtk-viewer-1.0.0-SNAPSHOT.war to the destination subfolder apache-tomcat/webapps/ROOT/
14. create destination subfolder dbvtk-data/
15. start solr server (on linux: solr/bin/solr start -c)
16. convert Sakila SIARD2 to Solr (on linux: jre/linux/bin/java "-Dfile.encoding=UTF-8" "-Ddbvtk.workspace=./dbvtk-data/" -jar dbptk-app.jar -e solr -i siard-2 -if sakila.siard)
17. stop solr server (on linux: solr/bin/solr stop)
16. remove logs and reports generated during the Sakila conversion
17. remove HOW_TO_BUILD.md

# Releasing for specific operative systems

Linux:
1. complete setup steps above
4. remove start.bat and stop.bat
5. zip all files in the destination folder and distribute the ZIP, start and stop the server by executing `start.sh` and `stop.sh`

Mac OS X:
1. complete setup steps above
2. rename start.sh to start.command
3. rename stop.sh to stop.command
4. remove start.bat and stop.bat
5. zip all files in the destination folder and distribute the ZIP, start and stop the server by double clicking `start.command` and `stop.command`

Windows:
1. complete setup steps above
4. remove start.sh and stop.sh
5. zip all files in the destination folder and distribute the ZIP, start and stop the server by double clicking `start.bat` and `stop.bat`
