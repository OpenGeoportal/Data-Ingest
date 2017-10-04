package org.opengeoportal.dataingest.api;

import com.github.geowarin.junit.DockerRule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by joana on 15/02/17.
 */

public class CacheServiceTest {

    private static String uri = "http://localhost:{PORT}/geoserver/";
    @ClassRule
    public static DockerRule CacheDockerRule = DockerRule.builder()
            .image("doublebyte/geoserver:squash").ports("8080")
            .waitForLog("Reloading user/groups successful").build();

    @BeforeClass
    public static void FixGeoserverUrl() throws InterruptedException {
        Thread.sleep(10000);
        final int port = CacheServiceTest.CacheDockerRule
                .getHostPort("8080/tcp");
        CacheServiceTest.uri = CacheServiceTest.uri.replace("{PORT}",
                Integer.toString(port));
    }

    private final String[] workspace = { "topp", "sf", "cite", "tiger" };

    @Test
    public void clearCache() throws Exception {

    }

    private List<Map<String, String>> getDatasets() throws Exception {

        final CacheService cs = new CacheService(uri);

        GeoserverDataStore ds = null;
        final List<Map<String, String>> hDatasets = new ArrayList<>();

        try {
            ds = new GeoserverDataStore(CacheServiceTest.uri);

            final String[] workspace_typenames = ds.typenames();
            for (final String typeName : workspace_typenames) {
                hDatasets.add(cs.getDataset(ds, typeName));
            }

        } catch (final java.lang.Exception e) {
            throw new Exception("Could not create WFS datastore " + "at: "
                    + CacheServiceTest.uri + ". Make sure it is up and "
                    + "running and that the connection settings are correct!");
        }

        return hDatasets;
    }

    @Test
    public void getInfo() throws Exception {

    }

    @Test
    public void getTitles() throws Exception {

        Assert.assertTrue(this.getDatasets().equals(this.getDatasets()));
    }

}
