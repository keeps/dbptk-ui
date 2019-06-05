#!/bin/sh

# run extension scripts
DIR=/docker-entrypoint.d

if [ -d "$DIR" ]
then
  /bin/run-parts "$DIR"
fi

if [ $# -eq 0 ] ; then
    echo 'Starting'
    exec java -Djava.security.egd=file:/dev/./urandom -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar /app.war
fi

exec $@
