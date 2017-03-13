package org.opengeoportal.dataingest.api;

import com.github.geowarin.junit.DockerRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertTrue;

/**
 * Created by joana on 15/02/17.
 */


public class CacheServiceTest {

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


    //Helper function to retrieve a random workspace
    private int getRandomWorkspace() {
        return ThreadLocalRandom.current().nextInt(1, workspace.length);
    }

    @Test
    public void getTitles() throws Exception {

        CacheService cs = new CacheService();

        assertTrue(cs.getDatasets(uri).equals(cs.getDatasets(uri)));
    }

    @Test
    public void getInfo() throws Exception {


    }

    @Test
    public void clearCache() throws Exception {

    }


}
