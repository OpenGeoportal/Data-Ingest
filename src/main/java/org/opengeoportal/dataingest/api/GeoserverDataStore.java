package org.opengeoportal.dataingest.api;

import org.geotools.data.FeatureSource;
import org.geotools.data.ResourceInfo;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.opengeoportal.dataingest.api.download.WFSClient;
import org.opengeoportal.dataingest.exception.GeoServerDataStoreException;
import org.opengeoportal.dataingest.exception.WFSException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
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
     * url subset, used to create the OWS WFS end point.
     */
    private static final String WFS_ENDPOINT = "ows?service=wfs";
    /**
     * url subset, used to create the OWS WMS end point.
     */
    private static final String WMS_ENDPOINT = "ows?service=wms";
    /**
     * Stores a Geotools WFS data store.
     */
    private WFSDataStore data;

    /**
     * Geoserver uri.
     */
    private String uri;

    /**
     * Constructor of the Geoserver data store. It fills two class member
     * variables, which store the WFS datastore and dataset (names, titles).
     *
     * @param aUri geoserver uri (include filter per workspace)
     * @throws Exception the exception
     */
    public GeoserverDataStore(final String aUri) throws GeoServerDataStoreException {

        uri = aUri;
        final String getCapabilities = uri + "wfs?REQUEST=GetCapabilities";

        final Map connectionParameters = new HashMap();
        connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL",
            getCapabilities);
        connectionParameters.put("WFSDataStoreFactory:TIMEOUT", TIMEOUT);

        try {
            final WFSDataStoreFactory dsf = new WFSDataStoreFactory();
            data = dsf.createDataStore(connectionParameters);

        } catch (final java.io.IOException ce) {
            throw new GeoServerDataStoreException("Could not connect to GeoServer " + "at: " + uri
                + ". Make sure it is up and "
                + "running and that the connection settings are correct!");
        }
    }

    public Map<String, String> getDataset(String typeName) throws Exception {

        try {
            // Make sure the list is empty
            final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
            featureSource = data.getFeatureSource(typeName);
            final ResourceInfo resourceInfo = featureSource.getInfo();
            Map<String, String> mDatasets = new HashMap<String, String>();
            mDatasets.put("name", resourceInfo.getName());
            mDatasets.put("title", resourceInfo.getTitle());
            mDatasets.put("geometry", featureSource.getSchema().getType(0).getBinding().getSimpleName());

            return mDatasets;

        } catch (IOException e) {
            throw new Exception(e.getMessage());
        } catch (final Exception ex) {
            throw ex;
        }


    }

    /**
     * Returns an array with the typenames in this datastore.
     *
     * @return typenames
     * @throws IOException
     */
    public String[] typenames() throws IOException {
        return data.getTypeNames();
    }

    /**
     * Returns the WFS datastore stored in a variable, initialized in the
     * constructor.
     *
     * @return WFS datastore
     */
    public WFSDataStore datastore() {
        return data;
    }

    /**
     * Wrapper for getLayerInfo, with a default value for bFeatureSize (false).
     *
     * @param workspace given workspace
     * @param dataset   given dataset
     * @return hashtable with layer properties
     * @throws WFSException the WFS exception
     * @throws Exception    the exception
     */
    public HashMap<String, String> getLayerInfo(final String workspace,
                                                final String dataset) throws WFSException,
        Exception {

        return getLayerInfo(workspace, dataset, false);

    }

    /**
     * Get detailed info about one layer.
     *
     * @param workspace    given workspace
     * @param dataset      given dataset
     * @param bFeatureSize boolean to indicate if we want to include the featureSize in the layer properties
     * @return hashtable with layer properties
     * @throws WFSException the WFS exception
     * @throws Exception    the exception
     */
    public HashMap<String, String> getLayerInfo(final String workspace,
                                                final String dataset, final boolean bFeatureSize) throws WFSException,
        Exception {

        final HashMap<String, String> layerProps = new HashMap<String, String>();
        final String typeName = workspace + ":" + dataset;

        try {
            final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = data
                .getFeatureSource(typeName);

            String geometry = featureSource.getSchema().getType(0).getBinding().getSimpleName();
            int noFeatures = featureSource.getFeatures().size(); // no features
            final WFSClient client = new WFSClient();
            final long fileSize = client.getFileSize(uri, workspace, dataset); // n.b.: this is the size of the
            // uncompressed file

            final ResourceInfo resourceInfo = featureSource.getInfo();

            layerProps.put("WFS", uri + WFS_ENDPOINT);
            layerProps.put("WMS", uri + WMS_ENDPOINT);
            layerProps.put("name", dataset);
            layerProps.put("workspace", workspace);
            layerProps.put("typename", resourceInfo.getName()); // typename
            layerProps.put("geometry", featureSource.getSchema().getType(0).getBinding().getSimpleName());
            if (bFeatureSize) layerProps.put("featureSize", String.valueOf(featureSource.getFeatures().size()));
            layerProps.put("size", String.valueOf(fileSize));
            layerProps.put("title", resourceInfo.getTitle());
            layerProps.put("description", resourceInfo.getDescription());
            layerProps.put("crs", resourceInfo.getCRS().toWKT().toString());
            layerProps.put("keywords", resourceInfo.getKeywords().toString());
            return layerProps;

        } catch (final java.io.IOException e) {
            throw new WFSException(e.getMessage() + " Maybe this is not a vector dataset?");
        } catch (final Exception e) {
            e.printStackTrace();
            throw new Exception("Could not read layer featuretype");
        }
    }

}
