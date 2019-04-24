#!/bin/bash

if [[ "$COPY_APP_ON_STARTUP" = "true" ]]; then
  echo "Replacing artifact with development version"

  rm -rf /usr/local/tomcat/webapps/ROOT
  cp -r /ROOT /usr/local/tomcat/webapps/ROOT

fi
