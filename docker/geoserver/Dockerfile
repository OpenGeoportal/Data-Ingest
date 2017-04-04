FROM winsent/geoserver:2.10

MAINTAINER doublebyte <joana.simoes@geocat.net>

ENV DATA_DIR $GEOSERVER_HOME/data_dir

# Copying and publishing the boston contours shapefile
RUN mkdir -p $DATA_DIR/workspaces/topp/boston_contours
RUN mkdir -p $DATA_DIR/workspaces/topp/boston_contours/boston_contours
RUN mkdir -p $DATA_DIR/data/boston_contours

ADD  ./boston_contours/boston_contours.tar.bz2 $DATA_DIR/data/boston_contours
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

COPY ./wait-for-it.sh /wait-for-it.sh
