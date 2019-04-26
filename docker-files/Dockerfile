FROM tomcat:8-jre8

LABEL application-name="Database Visualization Toolkit" \
      maintainer="admin@keep.pt" \
      vendor="KEEP SOLUTIONS"

EXPOSE 8080

# Dependencies + depencency cleanup + tomcat cleanup
RUN set -ex \
    ; apt-get -qq update \
    && apt-get -qq -y --no-install-recommends install \
       curl \
       unattended-upgrades \
       vim \
    && apt-get clean \
    && apt-get autoremove \
    ; rm -rf /var/lib/apt/lists/* \
    ; rm -rf /usr/local/tomcat/webapps/ROOT

COPY supervisor-conf.d/* /etc/supervisor/conf.d/

COPY /ROOT /usr/local/tomcat/webapps/ROOT

# environment
ENV DBVTK_RUNNING_IN_DOCKER=yes
ENV DBVTK_HOME=/dbvtk
#ENV SOLR_ZOOKEEPER_HOSTS="solr:9983"

# Fix configuration & do some final cleanup

RUN set -ex; sed -i -e 's/FINE/INFO/g' /usr/local/tomcat/conf/logging.properties

COPY /docker-entrypoint.sh /
COPY /docker-entrypoint.d/* /docker-entrypoint.d/

# Work-around to achieve optional copy
ONBUILD COPY /docker-entrypoint.d/* Dockerfile /docker-entrypoint.d/
ONBUILD RUN rm /docker-entrypoint.d/Dockerfile

ENTRYPOINT ["/docker-entrypoint.sh"]
