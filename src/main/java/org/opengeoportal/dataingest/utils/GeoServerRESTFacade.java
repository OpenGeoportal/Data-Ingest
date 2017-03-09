/*
 * @author Antonio
 */
package org.opengeoportal.dataingest.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;

/**
 * The Class GeoServerRESTFacade.
 */
public class GeoServerRESTFacade {
    
    /** The reader. */
    private GeoServerRESTReader reader;
    
    /** The publisher. */
    private GeoServerRESTPublisher publisher;
    
    /**
     * Instantiates a new geo server REST facade.
     */
    public GeoServerRESTFacade(String geoserverUrl, String geoserverUsername, String geoserverPassword) {
        try {
            this.reader = new GeoServerRESTReader(geoserverUrl, geoserverUsername, geoserverPassword);
            this.publisher = new GeoServerRESTPublisher(geoserverUrl, geoserverUsername, geoserverPassword);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }       
    }
    
    /**
     * Gets the layer.
     *
     * @param workspace the workspace
     * @param dataset the dataset
     * @return the layer
     */
    public RESTLayer getLayer(String workspace, String dataset) {
        return reader.getLayer(workspace, dataset);
    }
    
    /**
     * Gets the feature type.
     *
     * @param layer the layer
     * @return the feature type
     */
    public RESTFeatureType getFeatureType(RESTLayer layer) {
        return reader.getFeatureType(layer);
    }
    
    /**
     * Gets the datastore.
     *
     * @param featureType the feature type
     * @return the datastore
     */
    public RESTDataStore getDatastore(RESTFeatureType featureType) {
        return reader.getDatastore(featureType);
    }
    
    /**
     * Gets the datastore.
     *
     * @param workspace the workspace
     * @param dataset the dataset
     * @return the datastore
     */
    public RESTDataStore getDatastore(String workspace, String dataset) {
        return reader.getDatastore(getFeatureType(getLayer(workspace, dataset)));
    }

    /**
     * Unpublish feature type.
     *
     * @param workspace the workspace
     * @param storename the storename
     * @param layerName the layer name
     * @return true, if successful
     */
    public boolean unpublishFeatureType(String workspace, String storename, String layerName) {
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
    public boolean existsWorkspace(String workspace) {
        return reader.existsWorkspace(workspace);
    }

    /**
     * Exists datastore.
     *
     * @param workspace the workspace
     * @param datastore the datastore
     * @return true, if successful
     */
    public boolean existsDatastore(String workspace, String datastore) {
        return reader.existsDatastore(workspace, datastore);
    }

    /**
     * Publish a shape file.
     *
     * @param workspace the workspace
     * @param storeName the store name
     * @param datasetName the dataset name
     * @param shapefile the shapefile
     * @param srs the srs
     * @throws FileNotFoundException the file not found exception
     */
    public void publishShp(String workspace, String storeName, String datasetName,
            File shapefile, String srs) throws FileNotFoundException {
        publisher.publishShp(workspace, storeName, datasetName, shapefile, srs);
        
    }

    /**
     * Exists layer.
     *
     * @param workspace the workspace
     * @param name the name
     * @param quietonNotFound the quieton not found
     * @return true, if successful
     */
    public boolean existsLayer(String workspace, String name, boolean quietonNotFound) {
        return reader.existsLayer(workspace, name, quietonNotFound);
    }

}
