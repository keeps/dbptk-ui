#!/bin/bash

sed -i "s/%%CONTEXT_PATH%%/$CONTEXT_PATH/" /usr/local/tomcat/conf/server.xml
