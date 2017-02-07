FROM winsent/geoserver:2.10

MAINTAINER doublebyte <joana.simoes@geocat.net>

ENV DATA_DIR $GEOSERVER_HOME/data_dir

# Copying and publishing the boston contours shapefile
RUN mkdir -p $DATA_DIR/workspaces/topp/boston_contours
RUN mkdir -p $DATA_DIR/workspaces/topp/boston_contours/boston_contours
RUN mkdir -p $DATA_DIR/data/boston_contours

COPY  ./boston_contours/boston_contours.zip $DATA_DIR/data
RUN unzip $DATA_DIR/data/boston_contours.zip -d $DATA_DIR/data/boston_contours && rm $DATA_DIR/data/boston_contours.zip

COPY  ./boston_contours/datastore.xml $DATA_DIR/workspaces/topp/boston_contours
COPY  ./boston_contours/layer.xml $DATA_DIR/workspaces/topp/boston_contours/boston_contours
COPY  ./boston_contours/featuretype.xml $DATA_DIR/workspaces/topp/boston_contours/boston_contours
COPY  ./boston_contours/topp_boston_contours.sld $DATA_DIR/styles
COPY  ./boston_contours/topp_boston_contours.xml $DATA_DIR/styles

# Copying and publishing the postgis tables
RUN mkdir -p $DATA_DIR/workspaces/db
RUN mkdir -p $DATA_DIR/workspaces/db/postgis

COPY ./db/namespace.xml $DATA_DIR/workspaces/db
COPY ./db/workspace.xml $DATA_DIR/workspaces/db
COPY ./db/datastore.xml $DATA_DIR/workspaces/db/postgis

ADD ./db/postgis $DATA_DIR/workspaces/db/postgis
