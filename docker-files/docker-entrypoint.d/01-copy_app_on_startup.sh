#!/bin/bash

if [[ ! -z "$COPY_APP_ON_STARTUP" ]]; then
  echo "Replacing artifact with development version"

  rm -rf /usr/local/tomcat/webapps/ROOT
  cp -r /ROOT /usr/local/tomcat/webapps/ROOT

  sed -i -e 's/FINE/INFO/g' /usr/local/tomcat/conf/logging.properties
fi
