FROM winsent/geoserver:2.10

MAINTAINER doublebyte <joana.simoes@geocat.net>

ENV DATA_DIR /opt/geoserver/data_dir

# Copying and publishing the boston contours shapefile
RUN mkdir -p $DATA_DIR/workspaces/topp/boston_contours
RUN mkdir -p $DATA_DIR/workspaces/topp/boston_contours/boston_contours
RUN mkdir -p $DATA_DIR/data/boston_contours

COPY  ./boston_contours.zip $DATA_DIR/data
RUN unzip $DATA_DIR/data/boston_contours.zip -d $DATA_DIR/data/boston_contours && rm $DATA_DIR/data/boston_contours.zip

COPY  ./datastore.xml $DATA_DIR/workspaces/topp/boston_contours
COPY  ./layer.xml $DATA_DIR/workspaces/topp/boston_contours/boston_contours
COPY  ./featuretype.xml $DATA_DIR/workspaces/topp/boston_contours/boston_contours
COPY  ./topp_boston_contours.sld $DATA_DIR/styles
COPY  ./topp_boston_contours.xml $DATA_DIR/styles

