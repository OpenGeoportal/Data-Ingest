package org.opengeoportal.dataingest.api;

import com.github.geowarin.junit.DockerRule;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opengeoportal.dataingest.api.download.WFSClient;
import org.opengeoportal.dataingest.utils.FileNameUtils;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by joana on 19/01/17.
 */
public class WFSClientTest {

    @ClassRule
    public static DockerRule CacheDockerRule =
        DockerRule.builder()
            .image("doublebyte/geoserver:squash")
            .ports("8080")
            .waitForLog("Reloading user/groups successful")
            .build();
    private static String workspace = "topp";
    private static String dataset = "tasmania_cities";
    final static String fileName = FileNameUtils.getFullPathZipFile(System.getProperty(
        "java.io.tmpdir"),
         dataset);
    private static String uri = "http://localhost:{PORT}/geoserver/";
    private long maxAllowableLockTime = 3600;
    private int fileSize = 1383;

    @BeforeClass
    public static void FixGeoserverUrl() throws InterruptedException {
        Thread.sleep(10000);
        int port = CacheDockerRule.getHostPort("8080/tcp");
        uri = uri.replace("{PORT}", Integer.toString(port));
    }

    @AfterClass
    public static void cleanup() throws Exception {
        File f = new File(fileName);
        if (f.exists()) FileUtils.forceDelete(f);
    }

    @Test
    public void getFile() throws Exception {
        WFSClient client = new WFSClient();
        File f = client.getFile(uri, fileName, maxAllowableLockTime);
        assertTrue(f.exists() && f.length() > 0);
    }

    @Test
    public void getFileSize() throws Exception {
        WFSClient client = new WFSClient();
        assertEquals(client.getFileSize(uri,workspace, dataset), fileSize);
    }
}
