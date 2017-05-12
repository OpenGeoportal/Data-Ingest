#!/bin/bash

sed -ie 's#geoserver.url=http://localhost:8081/geoserver/#geoserver.url=http://gs:8080/geoserver/#g' \
 src/main/resources/application.properties;

sed -ie 's#localSolr.url=http://localhost:8983/solr/ogp#localSolr.url=http://solr:8983/solr/ogp#g' \
 src/main/resources/application.properties

mvn package -DskipTests -Drelax && cp target/Data-Ingest-0.0.1-SNAPSHOT.jar docker/data-ingest/;

sed -ie 's#geoserver.url=http://gs:8080/geoserver/#geoserver.url=http://localhost:8081/geoserver/#g' \
 src/main/resources/application.properties

sed -ie 's#localSolr.url=http://solr:8983/solr/ogp#localSolr.url=http://localhost:8983/solr/ogp#g' \
 src/main/resources/application.properties

echo "ok";
