FROM mdillon/postgis:9.6

MAINTAINER doublebyte <joana.simoes@geocat.net>

RUN apt-get update && \
    apt-get install -y unzip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Bulk import of Shapefiles into PostGIS
COPY  ./dump.zip /tmp/dump.zip
RUN unzip /tmp/dump.zip -d /tmp && \
 rm /tmp/dump.zip

RUN echo "*:*:*:ogpharvester:ogpharvester" > /root/.pgpass && \
 chmod 0600 /root/.pgpass

ADD postgis.sh /docker-entrypoint-initdb.d/
