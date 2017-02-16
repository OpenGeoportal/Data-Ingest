package org.opengeoportal.dataingest.api;

import org.geotools.data.FeatureSource;
import org.geotools.data.ResourceInfo;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joana on 27/01/17.
 */
public class GeoserverDataStore {
    /**
     * Geoserver connection timeout.
     */
    private static final int TIMEOUT = 15000;
    /**
     * Stores a Geotools WFS data store.
     */
    private WFSDataStore data;
    /**
     * Stores a hasmap with dataset (names, titles).
     */
    private HashMap<String, String> hTitles;

    /**
     * Constructor of the Geoserver data store.
     * It fills two class member variables, which store the WFS datastore and
     * dataset (names, titles).
     *
     * @param uri geoserver uri (include filter per workspace)
     * @throws Exception
     */
    public GeoserverDataStore(String uri) throws Exception {

        String getCapabilities = uri + "wfs?REQUEST=GetCapabilities";

        Map connectionParameters = new HashMap();
        connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL",
            getCapabilities);
        connectionParameters.put("WFSDataStoreFactory:TIMEOUT", TIMEOUT);

        try {
            WFSDataStoreFactory dsf = new WFSDataStoreFactory();
            data = dsf.createDataStore(connectionParameters);

            String[] typeNames = data.getTypeNames();

            HashMap<String, String> mTtitles = new HashMap<String, String>();
            for (String typeName : typeNames) {
                FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = data
                    .getFeatureSource(typeName);
                ResourceInfo resourceInfo = featureSource.getInfo();
                mTtitles.put(resourceInfo.getName(), resourceInfo.getTitle());
            }

            hTitles = mTtitles;

        } catch (java.net.ConnectException ce) {
            throw new Exception("Could not connect to GeoServer " + "at: " + uri
                + ". Make sure it is up and "
                + "running and that the connection settings are correct!");
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Returns the names and titles stored in a class variable, initialized in the constructor.
     *
     * @return hasmap with dataset (names, titles)
     */
    public HashMap<String, String> titles() {
        return hTitles;
    }

    /**
     * Returns the WFS datastore stored in a variable, initialized in the constructor.
     *
     * @return WFS datastore
     */
    public WFSDataStore datastore() {
        return data;
    }

    /**
     * Get detailed info about one layer.
     *
     * @param uri       geoserver url
     * @param workspace given workspace
     * @param dataset   given dataset
     * @return hashtable with layer properties
     * @throws Exception the exception
     */
    public HashMap<String, String> getLayerInfo(String uri,
                                                String workspace, String dataset) throws Exception {

        HashMap<String, String> layerProps = new HashMap<String, String>();
        String typeName = workspace + ":" + dataset;
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = data
            .getFeatureSource(typeName);
        ResourceInfo resourceInfo = featureSource.getInfo();

        try {
            // Example properties
            layerProps.put("name", resourceInfo.getName()); // typename
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
