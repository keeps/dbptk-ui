#!/bin/sh

# run extension scripts
DIR=/docker-entrypoint.d

if [ -d "$DIR" ]
then
  /bin/run-parts "$DIR"
fi

if [ $# -eq 0 ] ; then
    echo 'Starting'
    exec java -Djava.security.egd=file:/dev/./urandom -jar /app.war
fi

exec $@
