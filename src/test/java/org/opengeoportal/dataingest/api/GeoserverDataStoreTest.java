package org.opengeoportal.dataingest.api;

import com.github.geowarin.junit.DockerRule;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by joana on 27/01/17.
 */
public class GeoserverDataStoreTest {

    public static String uri="http://localhost:{PORT}/geoserver/";

    @ClassRule
    public static DockerRule dockerRule =
        DockerRule.builder()
            .image("winsent/geoserver:2.10")
            .ports("8080")
            .waitForLog("Reloading user/groups successful")
            //.waitForPort("8080")
            .build();

    @Test
    public void createDataStore() throws Exception {

        int port = dockerRule.getHostPort("8080/tcp");
        uri = uri.replace("{PORT}", Integer.toString(port));

        String getCapabilities
            = uri + "/wfs?REQUEST=GetCapabilities";

        Map connectionParameters = new HashMap();
        connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL",
            getCapabilities);
        connectionParameters.put("WFSDataStoreFactory:TIMEOUT", 15000);

        WFSDataStore mockupDataStore;
        WFSDataStore testDataStore;

        try {
            WFSDataStoreFactory dsf = new WFSDataStoreFactory();
            mockupDataStore = dsf.createDataStore(connectionParameters);
        } catch (Exception e) {
            throw e;
        }

        GeoserverDataStore gds = new GeoserverDataStore(uri);
        testDataStore = gds.datastore();

        assertEquals(mockupDataStore.getCapabilitiesURL(), testDataStore.getCapabilitiesURL());


    }

    @Test
    public void getTitlesForDataStore() throws Exception {

    }

    @Test
    public void getLayerTitles() throws Exception {

    }

    @Test
    public void getLayerTitles1() throws Exception {

    }

    @Test
    public void getLayerInfo() throws Exception {

    }

}
