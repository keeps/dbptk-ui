#!/usr/bin/env bash

# move to script directory, source http://stackoverflow.com/a/246128/1483200
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
cd "$DIR" || (echo "could not change directory to $DIR" && exit)

# find file names, source http://unix.stackexchange.com/a/156326
set -- dbptk-app-*.jar
DBPTK_JAR="$1"
if [[ $DBPTK_JAR == "dbptk-app-*.jar"    ]]; then echo "Could not find dbptk jar file"; exit; fi

# usage:
# $1: text asking for input
# $2: variable in which to put the result
# $3: default value
#function readPort() {
#  echo -n "$1 (defaults to $3): "
#  read -r PORT
#  if [[ -z $PORT ]] ;then
#    export "$2"="$3"
#  else
#    export "$2"="$PORT"
#  fi
#}

#function extractSolr() {
#  unzip -q "$SOLR_ZIP" -d solr
#  cd solr/solr*
#  mv ./* ..
#  cd ../..
#  solr/bin/solr start -c -p $solrCloudPort
#  solr/server/scripts/cloud-scripts/zkcli.sh -zkhost 127.0.0.1:$solrZooPort -cmd upconfig -confname dbvtk_table    -confdir solr-configset/dbvtk_table
#  solr/server/scripts/cloud-scripts/zkcli.sh -zkhost 127.0.0.1:$solrZooPort -cmd upconfig -confname dbvtk_database -confdir solr-configset/dbvtk_database
#  solr/server/scripts/cloud-scripts/zkcli.sh -zkhost 127.0.0.1:$solrZooPort -cmd upconfig -confname dbvtk_searches -confdir solr-configset/dbvtk_searches
#}

#readPort "Select the Solr Cloud port" "solrCloudPort" 8983
#export solrZooPort=$((solrCloudPort + 1000))
#echo "Solr Cloud embed zookeeper will run on port $solrZooPort"
#
## readPort "Select the web interface access port" "webPort" 8080
#webPort=8080


# ask to reset solr data. default to false. if the user answers to reset, exit if solr.zip does not exist
#if [[ -d solr ]] ;then
#  read -p "Remove existing databases (Y/N, default: N) " -n 1 -r
#  echo
#  if [[ $REPLY =~ ^[Yy]$ ]] ;then
#    solr/bin/solr stop -all
#    rm -rf solr
#    extractSolr
#  else
#    solr/bin/solr stop -all
#    solr/bin/solr start -c -p $solrCloudPort
#  fi
#else
#  extractSolr
#fi

# ask to load sakila database
#read -p "Load sample database Sakila into Solr? (Y/N, default: Y) " -n 1 -r
#echo
#if [[ ! $REPLY =~ ^[Nn]$ ]] ;then
#  if [[ -f sakila.siard ]]; then
#    echo "running command: java \"-Dfile.encoding=UTF-8\" -jar \"$DBPTK_JAR\" -i siard-2 -if sakila.siard -e solr -ep $solrCloudPort -ezp $solrZooPort"
#    java "-Dfile.encoding=UTF-8" -jar "$DBPTK_JAR" -i siard-2 -if sakila.siard -e solr -ep $solrCloudPort -ezp $solrZooPort
#  else
#    echo "Could not find sakila.siard file. Continuing without loading Sakila."
#  fi
#fi

# start DBVTK

#echo "Starting Database Visualization Toolkit, press CTRL+C to gracefully stop the server"
#echo "java \"-Dfile.encoding=UTF-8\" -jar \"$JETTY_JAR\" --port $webPort \"$DBVTK_WAR\""
#java "-Dfile.encoding=UTF-8" -jar "$JETTY_JAR" --port $webPort "$DBVTK_WAR"

mkdir dbvtk-data

chmod +x apache-tomcat/bin/startup.sh
chmod +x apache-tomcat/bin/shutdown.sh
chmod +x apache-tomcat/bin/catalina.sh

(apache-tomcat/bin/startup.sh)

echo "The Database Visualization Toolkit will be available at: http://127.0.0.1:8080"
echo
echo "To add databases use the following command:"
echo "java -jar \"-Dfile.encoding=UTF-8\" \"$DIR/$DBPTK_JAR\" -i siard-2 -if path/to/siard/file -e solr -ep 8983 -ezp 9983"
