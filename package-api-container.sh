#!/bin/bash

sed -i 's#http://localhost:8081/geoserver/#http://gs:8080/geoserver/#g' \
 src/main/resources/application.properties;

sed -i 's#http://localhost:8983/solr/ogp#http://solr:8983/solr/ogp#g' \
 src/main/resources/application.properties

sed -i 's#http://localhost:8080#http://tomcat:8082#g' \
 src/main/resources/application.properties

mvn package -DskipTests -Drelax && cp target/Data-Ingest-0.0.1-SNAPSHOT.jar docker/data-ingest/;

sed -i 's#http://gs:8080/geoserver/#http://localhost:8081/geoserver/#g' \
 src/main/resources/application.properties

sed -i 's#http://solr:8983/solr/ogp#http://localhost:8983/solr/ogp#g' \
 src/main/resources/application.properties

sed -i 's#http://tomcat:8082#http://localhost:8080#g' \
 src/main/resources/application.properties

echo "ok";
