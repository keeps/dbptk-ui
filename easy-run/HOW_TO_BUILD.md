NOTE: These are technical instructions to build the distribution package.
      If you just want to run the Database Visualization Toolkit, see the README.md file in this folder.

1. In DBPTK `mvn install -Pcommon`
2. In DBVTK `mvn install -Pcommon`
3. In DBPTK `mvn package`
4. In DBVTK `mvn package`
5. create a folder where the distribution package will be assembled, henceforth called the destination folder
6. copy scripts in DBPTK `easy-run` folder to the destination folder
7. download appropriate solr version from [here](http://lucene.apache.org/solr/downloads.html) and move it to the destination folder
8. download tomcat8 zip from [here](https://tomcat.apache.org/download-80.cgi) and extract it to a folder named `apache-tomcat`
9. copy dbvtk-model/src/main/resources/solr-configset to the destination folder
10. copy sakila.siard (the SIARD2 version of the Sakila database) to the destination folder
11. copy dbptk-core/target/dbptk-app-X.Y.Z.jar to the destination folder
12. delete the contents of apache-tomcat/webapps/ folder (that should exist inside the destination folder)
12. extract dbvtk-viewer/target/dbvtk-viewer-1.0.0-SNAPSHOT.war to the destination folder in subfolder apache-tomcat/webapps/ROOT
13. test
14. package in a zip file and distribute
