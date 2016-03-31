# Database Viewer

Relational Database Viewer for databases based on SIARD 2

### solr setup

1. download Solr 5.4.1
2. `{solr-dir}/bin/solr start -c`
3. `{solr-dir}/server/scripts/cloud-scripts/zkcli.sh -cmd upconfig -zkhost 127.0.0.1:9983 -confname dblist -confdir {project-root}/solr-config/dblist/conf` (9983 is by default the port used by solr + 1000)
4. `{solr-dir}bin/solr create -c dblist`
