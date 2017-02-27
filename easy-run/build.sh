#!/bin/bash

# Purpose:
# This file is used to automate the process of creating the deliverables that are part of each release:
# - Easy-run DBVTK zip file specific for each supported operating system (windows, linux, mac)
# - Easy-run DBVTK zip file containing the files for all supported operating systems
# - DBVTK war file to be added to a Java webserver (Jetty, tomcat, etc)
# - MD5, SHA1 and SHA512 checksums for the 5 files above.
# File types: 4 ZIP, 1 WAR, 3 CHECKSUM.

# NOTES:
# The Sakila SIARD can be generated from the Sakila MySQL database or the one at
# https://github.com/eark-project/ipres16-db-preservation-pack/blob/master/ipres16-workshop08/sakila_siard2.siard
#
# Other databases may also be used as an example (does not need to specifically be the Sakila database).
#
# You can find the JRE inside the dbvtk-*-all.zip file in the latest DBVTK release
# Look for it in here: https://github.com/keeps/db-visualization-toolkit/releases

echo "Options:"
echo "db-preservation-toolkit source directory: $1"
echo "db-visualization-toolkit source directory: $2"
echo "working directory: $3"
echo "dbptk version: $4"
echo "dbvtk version: $5"
echo "solr version: $6"
echo "sakila sample database path: $7"
echo "JRE folder (containing linux, mac, windows folders): $8"
echo ""
echo "WARNING: There should be no Solr server on port 8983"
echo "WARNING: Any existing files in $3 will be DELETED!"

read -r -p "Proceed with this configuration? [y/N] " response
case "$response" in
    [yY][eE][sS]|[yY])
      echo "Starting..."
      ;;
    *)
      echo "No changes were made."
      exit
      ;;
esac

# delete files in working directory
rm -rf "$3"; mkdir "$3"

# remove installed/cached packages
rm -rf ~/.m2/repository/com/databasepreservation

# build common parts
cd "$1"; mvn install -Pcommon
cd "$2"; mvn install -Pcommon
cd "$1"; mvn install -Pcommon

# build dbptk jar and dbvtk war
cd "$1"; mvn clean package
cd "$2"; mvn clean package

# create a folder where the distribution package will be assembled, henceforth called the destination folder
mkdir -p "$3/"

# copy everything in DBVTK `easy-run` folder to the destination folder
cp "$2/easy-run/README.md" "$3/"
cp "$2/easy-run/start.bat" "$3/"
cp "$2/easy-run/stop.bat" "$3/"
cp "$2/easy-run/start.sh" "$3/"
cp "$2/easy-run/stop.sh" "$3/"
cp "$2/easy-run/start.sh" "$3/start.command"
cp "$2/easy-run/stop.sh" "$3/stop.command"

# download appropriate solr version from [here](http://lucene.apache.org/solr/downloads.html)
echo "Downloading Solr from http://archive.apache.org/dist/lucene/solr/$6/solr-$6.tgz"
cd "$3"; curl -o solr.tgz http://archive.apache.org/dist/lucene/solr/$6/solr-$6.tgz

# extract it in the destination folder to a subfolder called solr
cd "$3"
tar -xzf "$3/solr.tgz"
rm "$3/solr.tgz"
mv "solr-$6" "solr"

# remove folder docs and example from solr directory
rm -rf solr/server/solr/configsets/sample_techproducts_configs
rm -rf solr/example
rm -rf solr/docs

# download tomcat8 and extract it to a folder named `apache-tomcat`
echo "Downloading tomcat from http://mirrors.fe.up.pt/pub/apache/tomcat/tomcat-8/v8.5.11/bin/apache-tomcat-8.5.11.tar.gz"
cd "$3"; curl -O http://mirrors.fe.up.pt/pub/apache/tomcat/tomcat-8/v8.5.11/bin/apache-tomcat-8.5.11.tar.gz
tar -xzf apache-tomcat-8.5.11.tar.gz
rm apache-tomcat-8.5.11.tar.gz
mv "apache-tomcat-8.5.11" "apache-tomcat"

# copy dbptk-core/target/dbptk-app-X.Y.Z.jar to the destination folder and rename it to dbptk-app.jar
cp "$1/dbptk-core/target/dbptk-app-$4.jar" "$3/dbptk-app.jar"

# delete the contents of (destination folder)/apache-tomcat/webapps/ folder
rm -rf "$3/apache-tomcat/webapps/";
mkdir "$3/apache-tomcat/webapps/"

# create destination subfolder apache-tomcat/webapps/ROOT/
mkdir "$3/apache-tomcat/webapps/ROOT/"
cd "$3/apache-tomcat/webapps/ROOT/"

# extract DBVTK .war to the ROOT folder
jar xf "$2/dbvtk-viewer/target/dbvtk-viewer-$5.war"

# chaging to working directory
cd "$3"

# copy war to working directory
cp "$2/dbvtk-viewer/target/dbvtk-viewer-$5.war" ./

# create dbvtk-data folder
mkdir dbvtk-data

# download sample database
#echo "Downloading Sakila SIARD from GitHub"
#curl -LJo sakila.siard "https://github.com/eark-project/ipres16-db-preservation-pack/blob/46aea622130ddaa91f9b34234fc2e7af2a83ad42/ipres16-workshop08/sakila_siard2.siard?raw=true"

# start solr server
solr/bin/solr start -c

# convert Sakila SIARD2 to Solr
java "-Dfile.encoding=UTF-8" "-Ddbvtk.home=./dbvtk-data/" -jar dbptk-app.jar -e solr -i siard-2 -if "$7"

# remove database file (no longer needed)
#rm sakila.siard

# stop solr server (on linux: solr/bin/solr stop)
solr/bin/solr stop

# remove logs and reports generated during the Sakila conversion
cd "$3"
rm -rf "solr/server/logs"

# copy JRE into working directory
cp -R $8 ./

# create ZIP files
zip -r dbvtk-$5-all.zip apache-tomcat/ dbvtk-data/ jre/ solr/ dbptk-app.jar start.bat start.command start.sh stop.bat stop.command stop.sh

cp dbvtk-$5-all.zip dbvtk-$5-windows.zip
zip -d dbvtk-$5-windows.zip \
  jre/linux\* jre/mac\* start.command start.sh stop.command stop.sh

cp dbvtk-$5-all.zip dbvtk-$5-mac.zip
zip -d dbvtk-$5-mac.zip \
  jre/linux\* jre/windows\* start.bat start.sh stop.bat stop.sh

cp dbvtk-$5-all.zip dbvtk-$5-linux.zip
zip -d dbvtk-$5-linux.zip \
  jre/mac\* jre/windows\* start.command start.bat stop.command stop.bat

# create checksums
shasum -a 1 dbvtk*.zip dbvtk*.war > dbvtk-$5.sha1
shasum -a 256 dbvtk*.zip dbvtk*.war > dbvtk-$5.sha256
md5sum dbvtk*.zip dbvtk*.war > dbvtk-$5.md5
