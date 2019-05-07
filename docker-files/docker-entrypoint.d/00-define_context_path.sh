#!/bin/bash

sed -i -r "s%</Host>%<Context docBase=\"/dbvtk.war\" path=\"$CONTEXT_PATH\" reloadable=\"true\"/></Host>%" /usr/local/tomcat/conf/server.xml
