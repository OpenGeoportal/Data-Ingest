package org.opengeoportal.dataingest.api;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.github.geowarin.junit.DockerRule;

public class DownloadTest {
    
    private static String uri = "http://localhost:{PORT}/geoserver/";
    private String[] workspace = {"topp", "sf", "cite", "tiger"};
    private String[] dataset = {"tasmania_cities", "tasmania_roads"};
    
    @ClassRule
    public static DockerRule CacheDockerRule =
        DockerRule.builder()
            .image("doublebyte/geoserver:squash")
            .ports("8080")
            .waitForLog("Reloading user/groups successful")
            .build();


    @BeforeClass
    public static void FixGeoserverUrl() throws InterruptedException {
        Thread.sleep(10000);
        int port = CacheDockerRule.getHostPort("8080/tcp");
        uri = uri.replace("{PORT}", Integer.toString(port));
    }


    @Test
    public void testFileDownload() throws Exception {

    }

}
