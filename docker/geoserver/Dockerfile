FROM winsent/geoserver:2.10

MAINTAINER doublebyte <joana.simoes@geocat.net>

ENV DATA_DIR $GEOSERVER_HOME/data_dir

# url of the large file, so we can easily change it
ENV URL https://eos.geocat.net/owncloud/public.php?service=files&t=812601ac9b8d1d5abb82ef943d160825&download

RUN apt-get update -q && \
    apt-get install -y -qq curl && \
    apt-get clean

# Copying and publishing the boston contours shapefile
RUN mkdir -p $DATA_DIR/workspaces/topp/boston_contours
RUN mkdir -p $DATA_DIR/workspaces/topp/boston_contours/boston_contours
RUN mkdir -p $DATA_DIR/data/boston_contours

WORKDIR $DATA_DIR/data/boston_contours/
RUN curl -J -O "${URL}" &&\
 tar xvjf boston_contours.tar.bz2  &&\
 rm -rf boston_contours.tar.bz2
COPY ./boston_contours/datastore.xml $DATA_DIR/workspaces/topp/boston_contours
ADD  ./boston_contours/feature.tar.bz2 $DATA_DIR/workspaces/topp/boston_contours/boston_contours
ADD  ./boston_contours/style.tar.bz2 $DATA_DIR/styles

# Copying and publishing the postgis tables

RUN mkdir -p $DATA_DIR/workspaces/db
COPY ./db/namespace.xml $DATA_DIR/workspaces/db
COPY ./db/workspace.xml $DATA_DIR/workspaces/db
ADD ./postgis.tar.bz2 $DATA_DIR/workspaces/db

RUN mkdir -p $DATA_DIR/workspaces/db2
COPY ./db2/namespace.xml $DATA_DIR/workspaces/db2
COPY ./db2/workspace.xml $DATA_DIR/workspaces/db2
ADD ./postgis2.tar.bz2 $DATA_DIR/workspaces/db2

WORKDIR /
COPY ./wait-for-it.sh /wait-for-it.sh
