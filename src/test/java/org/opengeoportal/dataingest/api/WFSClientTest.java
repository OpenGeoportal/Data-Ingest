package org.opengeoportal.dataingest.api;

import org.junit.Test;

/**
 * Created by joana on 19/01/17.
 */
public class WFSClientTest {

    @Test
    public void getFile() throws Exception {

        //TODO: write this test against docker mockup
/*
        String uri = "http://localhost:8081/geoserver" +
            "/workspace/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=workspace:dataset&outputFormat=SHAPE-ZIP";

        try {

            WFSClient client = new WFSClient();

            String fileName = FileNameUtils.getFullPathZipFile("test", "test");
            File file = client.getFile(uri, fileName);

            throw new Exception();

        } catch (RestClientException e1) {
            // It's ok
        }

*/
    }

}
