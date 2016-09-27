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

# setup environment
DBPTK=dbptk-app.jar
export DBVTK_WORKSPACE=$DIR/dbvtk-data
export JAVA_HOME=$DIR/jre/linux
[ "$(uname -s)" = "Darwin" ] && export JAVA_HOME=$DIR/jre/mac
export CATALINA_HOME=$DIR/apache-tomcat

chmod +x apache-tomcat/bin/startup.sh
chmod +x apache-tomcat/bin/shutdown.sh
chmod +x apache-tomcat/bin/catalina.sh

# start
solr/bin/solr start -c
apache-tomcat/bin/startup.sh

# provide instructions
echo "The Database Visualization Toolkit will be available at: http://127.0.0.1:8080"
echo
echo "To add databases use the following command:"
echo "$JAVA_HOME/bin/java -jar \"-Dfile.encoding=UTF-8\" \"-Ddbvtk.workspace=$DBVTK_WORKSPACE\" \"$DIR/$DBPTK\" -e solr -i siard-2 -if path/to/siard_2/file"
