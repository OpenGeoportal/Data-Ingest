package org.opengeoportal.dataingest.api;

import com.github.geowarin.junit.DockerRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertTrue;

/**
 * Created by joana on 15/02/17.
 */
public class CacheServiceTest {

    public static String uri = "http://localhost:{PORT}/geoserver/";
    private String[] workspace = {"topp","sf","cite","tiger"};
    private String[] dataset = {"states","boston_contours","tasmania_cities","tasmania_roads"};

    @ClassRule
    public static DockerRule dockerRule =
        DockerRule.builder()
            .image("winsent/geoserver:2.10")
            .ports("8080")
            .waitForLog("Reloading user/groups successful")
            .build();

    public CacheServiceTest() {
        int port = dockerRule.getHostPort("8080/tcp");
        uri = uri.replace("{PORT}", Integer.toString(port));
    }

    //Helper function to retrieve a random workspace
    private int getRandomWorkspace(){
        return ThreadLocalRandom.current().nextInt(1, workspace.length);
    }

    @Test
    public void getTitles() throws Exception {

        CacheService cs= new CacheService();

        int num=getRandomWorkspace();

        assertTrue(cs.getTitles(uri).equals(cs.getTitles(uri)) );
    }

    @Test
    public void getInfo() throws Exception {


    }

    @Test
    public void clearCache() throws Exception {

    }

}
