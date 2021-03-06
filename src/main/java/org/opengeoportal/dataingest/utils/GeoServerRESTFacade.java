package org.opengeoportal.dataingest.utils;

import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.decoder.RESTLayerGroup;
import it.geosolutions.geoserver.rest.decoder.RESTLayerGroupList;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The Class GeoServerRESTFacade.
 */
public class GeoServerRESTFacade {

    /**
     * The reader.
     */
    private GeoServerRESTReader reader;

    /**
     * The publisher.
     */
    private GeoServerRESTPublisher publisher;

    /**
     * Instantiates a new geo server REST facade.
     *
     * @param geoserverUrl      the geoserver url
     * @param geoserverUsername the geoserver username
     * @param geoserverPassword the geoserver password
     */
    public GeoServerRESTFacade(final String geoserverUrl,
                               final String geoserverUsername, final String geoserverPassword) {
        try {
            this.reader = new GeoServerRESTReader(geoserverUrl,
                geoserverUsername, geoserverPassword);
            this.publisher = new GeoServerRESTPublisher(geoserverUrl,
                geoserverUsername, geoserverPassword);
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the layer.
     *
     * @param workspace the workspace
     * @param dataset   the dataset
     * @return the layer
     */
    public RESTLayer getLayer(final String workspace, final String dataset) {
        return reader.getLayer(workspace, dataset);
    }

    /**
     * Gets the feature type.
     *
     * @param layer the layer
     * @return the feature type
     */
    public RESTFeatureType getFeatureType(final RESTLayer layer) {
        return reader.getFeatureType(layer);
    }

    /**
     * Gets the datastore.
     *
     * @param featureType the feature type
     * @return the datastore
     */
    public RESTDataStore getDatastore(final RESTFeatureType featureType) {
        return reader.getDatastore(featureType);
    }

    /**
     * Gets the datastore.
     *
     * @param workspace the workspace
     * @param dataset   the dataset
     * @return the datastore
     */
    public RESTDataStore getDatastore(final String workspace,
                                      final String dataset) {
        return reader
            .getDatastore(getFeatureType(getLayer(workspace, dataset)));
    }

    /**
     * Unpublish feature type.
     *
     * @param workspace the workspace
     * @param storename the storename
     * @param layerName the layer name
     * @return true, if successful
     */
    public boolean unpublishFeatureType(final String workspace,
                                        final String storename, final String layerName) {
        return publisher.unpublishFeatureType(workspace, storename, layerName);
    }

    /**
     * Reload.
     */
    public void reload() {
        publisher.reload();
    }

    /**
     * Exists workspace.
     *
     * @param workspace the workspace
     * @return true, if successful
     */
    public boolean existsWorkspace(final String workspace) {
        return reader.existsWorkspace(workspace);
    }

    /**
     * Exists datastore.
     *
     * @param workspace the workspace
     * @param datastore the datastore
     * @return true, if successful
     */
    public boolean existsDatastore(final String workspace,
                                   final String datastore) {
        return reader.existsDatastore(workspace, datastore);
    }

    /**
     * Publish a shape file.
     *
     * @param workspace   the workspace
     * @param storeName   the store name
     * @param datasetName the dataset name
     * @param shapefile   the shapefile
     * @param srs         the srs
     * @return true, if successful
     * @throws FileNotFoundException the file not found exception
     */
    public boolean republishShp(final String workspace, final String storeName,
                                final String datasetName, final File shapefile, final String srs)
        throws FileNotFoundException {

        if (publisher.unpublishFeatureType(workspace, storeName, datasetName)) {
            return publisher.publishShp(workspace, storeName, datasetName, shapefile, srs);
        } else {
            return false;
        }

    }

    /**
     * Publish a shape file.
     *
     * @param workspace   the workspace
     * @param storeName   the store name
     * @param datasetName the dataset name
     * @param shapefile   the shapefile
     * @param srs         the srs
     * @return true, if successful
     * @throws FileNotFoundException the file not found exception
     */
    public boolean publishShp(final String workspace, final String storeName,
                              final String datasetName, final File shapefile, final String srs)
        throws FileNotFoundException {

        return publisher.publishShp(workspace, storeName, datasetName, shapefile, srs);

    }

    /**
     * Exists layer.
     *
     * @param workspace       the workspace
     * @param name            the name
     * @param quietonNotFound the quieton not found
     * @return true, if successful
     */
    public boolean existsLayer(final String workspace, final String name,
                               final boolean quietonNotFound) {
        return reader.existsLayer(workspace, name, quietonNotFound);
    }

    /**
     * Find the layer groups for a given layer.
     *
     * @param ws   workspace
     * @param name layer name
     * @return a string set with the layer groups for this layer
     */
    public Set<String> getLayerGroupsForLayer(final String ws, final String name) {

        Set<String> sLG = new HashSet<>();

        //loop through layer groups in this workspace
        RESTLayerGroupList rgl = reader.getLayerGroups();
        rgl.forEach((lg) -> {
            String groupName = lg.getName();
            System.out.println(groupName);
            if (isLayerInLayerGroup(lg.getName(), name)) sLG.add(groupName);
        });

        return sLG;
    }

    /**
     * Utility function to check if a layer is in a given group.
     *
     * @param layerGroup a layer group
     * @param name       a layer name
     * @return boolean variable stating if the layer belongs to this group.
     */
    private boolean isLayerInLayerGroup(final String layerGroup, final String name) {

        RESTLayerGroup lg = reader.getLayerGroup(layerGroup);

        Iterator<it.geosolutions.geoserver.rest.decoder.RESTPublished> it = lg.getPublishedList().iterator();
        while (it.hasNext()) {
            String gName = it.next().getName();
            System.out.println(gName);
            if (gName.compareTo(name) == 0) return true;
        }
        return false;
    }


}
