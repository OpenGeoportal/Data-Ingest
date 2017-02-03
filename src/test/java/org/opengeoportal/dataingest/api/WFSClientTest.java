package org.opengeoportal.dataingest.api;

import java.io.File;

import org.junit.Test;
import org.opengeoportal.dataingest.api.download.WFSClient;
import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.springframework.web.client.RestClientException;

/**
 * Created by joana on 19/01/17.
 */
public class WFSClientTest {

    @Test
    public void getFile() throws Exception {

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


    }

}
