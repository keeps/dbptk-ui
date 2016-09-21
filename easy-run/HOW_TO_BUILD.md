NOTE: These are technical instructions to build the distribution package.
      The instructions to run the Database Visualization Toolkit, see the README.md file in this folder.

1. In DBPTK `mvn install -Pcommon`
2. In DBVTK `mvn install -Pcommon`
3. In DBPTK `mvn package`
4. In DBVTK `mvn package`
5. create a folder where the distribution package will be assembled, henceforth called the destination folder
6. copy scripts and README.md in DBPTK `easy-run` folder to the destination folder
7. download appropriate solr version from [here](http://lucene.apache.org/solr/downloads.html) and extract it in the destination folder to a subfolder called solr
8. download tomcat8 zip from [here](https://tomcat.apache.org/download-80.cgi) and extract it to a folder named `apache-tomcat`
9. copy dbvtk-model/src/main/resources/solr-configset to the destination folder
10. copy dbptk-core/target/dbptk-app-X.Y.Z.jar to the destination folder
11. delete the contents of (destination folder)/apache-tomcat/webapps/ folder
12. extract dbvtk-viewer/target/dbvtk-viewer-1.0.0-SNAPSHOT.war to the destination folder in subfolder apache-tomcat/webapps/ROOT
13. run solr server (bin/solr start -c)
14. convert Sakila SIARD2 to Solr (java \"-Dfile.encoding=UTF-8\" -jar dbptk-app-X.Y.Z.jar -i siard-2 -if sakila.siard -e solr)
15. stop solr server (bin/solr stop)
16. package zip and distribute
