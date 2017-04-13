package org.opengeoportal.dataingest.api;

import com.github.geowarin.junit.DockerRule;
import org.geotools.data.FeatureSource;
import org.geotools.data.ResourceInfo;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opengeoportal.dataingest.api.download.WFSClient;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by joana on 27/01/17.
 */

public class GeoserverDataStoreTest {

    private static String uri = "http://localhost:{PORT}/geoserver/";
    private String workspace = "topp";
    private String dataset = "tasmania_cities";
    private WFSDataStore mockupDataStore;


    @ClassRule
    public static DockerRule DataStoreDockerRule =
        DockerRule.builder()
            .image("doublebyte/geoserver:squash")
            .ports("8080")
            .waitForLog("Reloading user/groups successful")
            .build();

    @BeforeClass
    public static void hold() throws InterruptedException {
        Thread.sleep(10000);
    }

    @Before
    public void createDatastore() throws Exception {

        int port = DataStoreDockerRule.getHostPort("8080/tcp");
        uri = uri.replace("{PORT}", Integer.toString(port));

        String getCapabilities
            = uri + "wfs?REQUEST=GetCapabilities";

        Map connectionParameters = new HashMap();
        connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL",
            getCapabilities);
        connectionParameters.put("WFSDataStoreFactory:TIMEOUT", 15000);

        try {
            WFSDataStoreFactory dsf = new WFSDataStoreFactory();
            mockupDataStore = dsf.createDataStore(connectionParameters);
        } catch (Exception e) {
            throw e;
        }

    }


    @Test
    public void datastore() throws Exception {

        GeoserverDataStore gds = new GeoserverDataStore(uri,false);
        WFSDataStore testDataStore = gds.datastore();

        assertEquals(mockupDataStore.getCapabilitiesURL(), testDataStore.getCapabilitiesURL());
    }

    @Test
    public void datasets() throws Exception {

        String[] typeNames = mockupDataStore.getTypeNames();

        List<Map<String, String>> hDatasets = new ArrayList<Map<String, String>>();
        for (String typeName : typeNames) {
            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = mockupDataStore
                .getFeatureSource(typeName);
            ResourceInfo resourceInfo = featureSource.getInfo();
            Map<String,String> mDatasets = new HashMap<String,String>();
            mDatasets.put("name", resourceInfo.getName());
            mDatasets.put("title", resourceInfo.getTitle());
            mDatasets.put("geometry", featureSource.getSchema().getType(0).getBinding().getSimpleName());
            hDatasets.add(mDatasets);
        }

        GeoserverDataStore gds = new GeoserverDataStore(uri, true);
        assertEquals(hDatasets, gds.datasets());
    }

    @Test
    public void getLayerInfo() throws Exception {

        HashMap<String, String> layerProps = new HashMap<String, String>();
        String typeName = workspace + ":" + dataset;
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = mockupDataStore
            .getFeatureSource(typeName);
        ResourceInfo resourceInfo = featureSource.getInfo();

        String geometry = featureSource.getSchema().getType(0).getBinding().getSimpleName();
        int noFeatures = featureSource.getFeatures().size(); // no features
        final WFSClient client = new WFSClient();
        final long fileSize = client.getFileSize(uri, workspace, dataset);

        layerProps.put("WFS", uri + "ows?service=wfs");
        layerProps.put("WMS", uri + "ows?service=wms");
        layerProps.put("name", dataset);
        layerProps.put("workspace", workspace);
        layerProps.put("typename", resourceInfo.getName()); // typename
        layerProps.put("geometry", featureSource.getSchema().getType(0).getBinding().getSimpleName());
        layerProps.put("size", String.valueOf(fileSize));
        layerProps.put("title", resourceInfo.getTitle());
        layerProps.put("description", resourceInfo.getDescription());
        layerProps.put("crs", resourceInfo.getCRS().toWKT().toString());
        layerProps.put("keywords", resourceInfo.getKeywords().toString());

        GeoserverDataStore gds = new GeoserverDataStore(uri, false);

        assertEquals(layerProps, gds.getLayerInfo(workspace, dataset));

    }

}
