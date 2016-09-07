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

# shutdown solr
echo "Shutting down Solr server"
solr/bin/solr stop -all

echo "Shutting down tomcat server"
apache-tomcat/bin/shutdown.sh

echo "Cleaning up"
rm -rf log
rm -f dbptk-app-*.log.txt
rm -f dbptk-report-*.txt

echo "Done."
