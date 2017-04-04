#!/bin/bash

while read p; do
  curl -u admin:geoserver -v -XPOST -H 'Content-Type:text/xml' -d '<featureType><name>'$p'</name></featureType>' http://localhost:8081/geoserver/rest/workspaces/db2/datastores/postgis2/featuretypes;
done < tablelist.txt 
