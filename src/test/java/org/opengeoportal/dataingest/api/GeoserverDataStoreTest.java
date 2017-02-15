package org.opengeoportal.dataingest.api;

import com.github.geowarin.junit.DockerRule;
import org.geotools.data.FeatureSource;
import org.geotools.data.ResourceInfo;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.junit.ClassRule;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by joana on 27/01/17.
 */
public class GeoserverDataStoreTest {

    public static String uri = "http://localhost:{PORT}/geoserver/";
    private String workspace = "topp";
    private String dataset = "states";

    @ClassRule
    public static DockerRule dockerRule =
        DockerRule.builder()
            .image("winsent/geoserver:2.10")
            .ports("8080")
            .waitForLog("Reloading user/groups successful")
            //.waitForPort("8080")
            .build();

    private WFSDataStore createMockupDataStore() throws Exception {

        int port = dockerRule.getHostPort("8080/tcp");
        uri = uri.replace("{PORT}", Integer.toString(port));

        String getCapabilities
            = uri + "/wfs?REQUEST=GetCapabilities";

        Map connectionParameters = new HashMap();
        connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL",
            getCapabilities);
        connectionParameters.put("WFSDataStoreFactory:TIMEOUT", 15000);

        try {
            WFSDataStoreFactory dsf = new WFSDataStoreFactory();
            return dsf.createDataStore(connectionParameters);
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void datastore() throws Exception {

        WFSDataStore mockupDataStore = createMockupDataStore();
        GeoserverDataStore gds = new GeoserverDataStore(uri);
        WFSDataStore testDataStore = gds.datastore();

        assertEquals(mockupDataStore.getCapabilitiesURL(), testDataStore.getCapabilitiesURL());
    }

    @Test
    public void titles() throws Exception {

        WFSDataStore mockupDataStore = createMockupDataStore();

        String[] typeNames = mockupDataStore.getTypeNames();

        HashMap<String, String> mTtitles = new HashMap<String, String>();
        for (String typeName : typeNames) {
            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = mockupDataStore
                .getFeatureSource(typeName);
            ResourceInfo resourceInfo = featureSource.getInfo();
            mTtitles.put(resourceInfo.getName(), resourceInfo.getTitle());
        }

        GeoserverDataStore gds = new GeoserverDataStore(uri);

        assertEquals(mTtitles, gds.titles());
    }

    @Test
    public void getLayerInfo() throws Exception {

        WFSDataStore mockupDataStore = createMockupDataStore();

        HashMap<String, String> layerProps = new HashMap<String, String>();
        String typeName = workspace + ":" + dataset;
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = mockupDataStore
            .getFeatureSource(typeName);
        ResourceInfo resourceInfo = featureSource.getInfo();

        // Example properties
        layerProps.put("name", resourceInfo.getName()); // typename
        layerProps.put("title", resourceInfo.getTitle());
        layerProps.put("description", resourceInfo.getDescription());
        layerProps.put("crs", resourceInfo.getCRS().toWKT().toString());
        layerProps.put("keywords", resourceInfo.getKeywords().toString());

        GeoserverDataStore gds = new GeoserverDataStore(uri);

        assertEquals(layerProps, gds.getLayerInfo(uri, workspace, dataset));

    }

}
