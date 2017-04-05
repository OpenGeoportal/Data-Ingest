FROM mdillon/postgis:9.6

MAINTAINER doublebyte <joana.simoes@geocat.net>

# url of the large file, so we can easily change it
ENV URL https://eos.geocat.net/owncloud/public.php?service=files&t=f3990527fdaeb84b6ec035560801e175&download

RUN apt-get update -q && \
    apt-get install -y -qq curl && \
    apt-get clean

# Bulk import of Shapefiles into PostGIS

WORKDIR /tmp
RUN curl -J -O "${URL}" &&\
 tar xvjf dump.tar.bz2  &&\
 rm -rf dump.tar.bz2

RUN echo "*:*:*:ogpharvester:ogpharvester" > /root/.pgpass && \
 chmod 0600 /root/.pgpass

WORKDIR /
ADD postgis.sh /docker-entrypoint-initdb.d/
