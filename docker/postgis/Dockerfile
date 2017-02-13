FROM mdillon/postgis:9.6

MAINTAINER doublebyte <joana.simoes@geocat.net>

# Bulk import of Shapefiles into PostGIS
ADD ./dump.tar.bz2 /tmp

RUN echo "*:*:*:ogpharvester:ogpharvester" > /root/.pgpass && \
 chmod 0600 /root/.pgpass

ADD postgis.sh /docker-entrypoint-initdb.d/
