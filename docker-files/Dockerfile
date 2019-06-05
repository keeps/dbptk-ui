FROM openjdk:8-jdk-alpine

LABEL application-name="Database Visualization Toolkit" \
      maintainer="admin@keep.pt" \
      vendor="KEEP SOLUTIONS"

VOLUME /tmp
EXPOSE 8080

# Dependencies + depencency cleanup + tomcat cleanup
# RUN set -ex \
#     ; apt-get -qq update \
#     && apt-get -qq -y --no-install-recommends install \
#        curl vim less jq netcat \
#        unattended-upgrades \
#     && apt-get clean \
#     && apt-get autoremove \
#     ; rm -rf /var/lib/apt/lists/* \
#     ; rm -rf /usr/local/tomcat/webapps/*


# Install app via server.xml so context path can easily be re-defined
COPY /*.war /app.war

# environment
ENV DBVTK_RUNNING_IN_DOCKER=yes
ENV DBVTK_HOME=/dbvtk
#ENV SOLR_ZOOKEEPER_HOSTS="solr:9983"

COPY /docker-entrypoint.sh /
COPY /docker-entrypoint.d/* /docker-entrypoint.d/

# Work-around to achieve optional copy
ONBUILD COPY /docker-entrypoint.d/* Dockerfile /docker-entrypoint.d/
ONBUILD RUN rm /docker-entrypoint.d/Dockerfile

ENTRYPOINT ["/docker-entrypoint.sh"]
