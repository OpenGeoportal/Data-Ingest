package org.opengeoportal;

import org.geotools.data.FeatureSource;
import org.geotools.data.ResourceInfo;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by joana on 27/01/17.
 */
public class GeoserverDataStore {
    /**
     * Geoserver connection timeout.
     */
    public static final int TIMEOUT = 15000;

    /**
     * Constructor of the Geoserver data store.
     */
    public GeoserverDataStore() {
    }

    /**
     * Creates a WFS data store.
     *
     * @param uri geoserver url
     * @return WFS data store
     * @throws Exception
     */
    public final WFSDataStore createDataStore(final String uri) throws
        Exception {

        String getCapabilities = uri
            + "/wfs?REQUEST=GetCapabilities";

        Map connectionParameters = new HashMap();
        connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL",
            getCapabilities);
        connectionParameters.put("WFSDataStoreFactory:TIMEOUT", TIMEOUT);

        try {
            WFSDataStoreFactory dsf = new WFSDataStoreFactory();
            return dsf.createDataStore(connectionParameters);
        } catch (java.net.ConnectException ce) {
            throw new Exception("Could not connect to GeoServer "
                + "at: " + uri + ". Make sure it is up and "
                + "running and that the connection settings are correct!");
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Lists layer names and titles, for a given data store.
     * @param data data store
     * @return layer names and titles as a hash table
     * @throws Exception
     */
    public final HashMap<String, String> getTitlesForDataStore(final WFSDataStore data)
        throws Exception {

        try {
            HashMap<String, String> hLayer = new HashMap<String, String>();
            String[] typeNames = data.getTypeNames();

            for (String typeName : typeNames) {
                FeatureSource<SimpleFeatureType, SimpleFeature>
                    featureSource = data.getFeatureSource(typeName);
                ResourceInfo resourceInfo = featureSource.getInfo();
                hLayer.put(typeName, resourceInfo
                    .getTitle());
            }
            return hLayer;
        } catch (IOException io) {
            throw new Exception("Could not read featuretype");
        } catch (Exception ex) {
            throw ex;
        }
    }


    /**
     * Lists layer titles for all workspaces.
     * @param uri geoserver url
     * @return layer names and titles as a hash table
     * @throws Exception
     */
    public final HashMap<String, String> getLayerTitles(final String uri) throws
        Exception {

        try {
            WFSDataStore data = createDataStore(uri);
            List<String> layerNames = new ArrayList<String>();
            return getTitlesForDataStore(data);
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Lists layer titles for a given workspace.
     * @param uri geoserver url
     * @param workspace given workspace
     * @return layer names and titles as a hash table
     * @throws Exception
     */
    public final HashMap<String, String> getLayerTitles(final String uri, final String
        workspace) throws
        Exception {

        try {
            WFSDataStore data = createDataStore(uri + "/" + workspace);
            List<String> layerNames = new ArrayList<String>();
            return getTitlesForDataStore(data);
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Get detailed info about one layer.
     * @param uri geoserver url
     * @param workspace given workspace
     * @param dataset given dataset
     * @return hashtable with layer properties
     * @throws Exception
     */
    public final HashMap<String, String> getLayerInfo(final String uri,
                                                final String workspace,
                                                final String dataset)
        throws Exception {

        WFSDataStore data = createDataStore(uri);
        HashMap<String, String> layerProps = new HashMap<String, String>();
        String typeName = workspace + ":" + dataset;
        FeatureSource<SimpleFeatureType, SimpleFeature>
            featureSource = data.getFeatureSource(typeName);
        ResourceInfo resourceInfo = featureSource.getInfo();

        try {
            //Example properties
            layerProps.put("title", resourceInfo.getTitle());
            layerProps.put("description", resourceInfo.getDescription());
            layerProps.put("crs", resourceInfo.getCRS().toWKT().toString());
            layerProps.put("keywords", resourceInfo.getKeywords().toString());
            return layerProps;

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Could not read layer featuretype");
        }
    }


}
