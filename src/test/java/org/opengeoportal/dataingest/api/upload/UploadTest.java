package org.opengeoportal.dataingest.api.upload;

import org.junit.Test;
import org.opengeoportal.dataingest.exception.GeoServerException;
import org.opengeoportal.dataingest.utils.ShapeFileValidator;
import org.opengeoportal.dataingest.utils.TicketGenerator;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UploadTest {

    private static String ROADS_SHAPEFILE = "./src/test/resources/roads.zip";
    private String geoserverUrl = "http://localhost:8081/geoserver/";
    private String geoserverUsername = "admin";
    private String geoserverPassword = "geoserver";

    @Test
    public void testShapeFile() throws Exception {

        File shpf = new File(ROADS_SHAPEFILE);
        boolean noException = false;

        try {
            assertEquals("EPSG:26713", ShapeFileValidator.isAValidShapeFile(shpf, true));
            noException = true;
        } catch (Exception e) {
            assertTrue(noException);
        }

    }

    @Test
    public void testUpload() throws Exception {
        File shpf = new File(ROADS_SHAPEFILE);

        long ticket = TicketGenerator.openATicket();

        UploadRequest up = new UploadRequest("topp", "airports", "airports", shpf, "strEpsg", ticket, false);

        RemoteUploadService rs = new RemoteUploadService();

        ReflectionTestUtils.setField(rs, "geoserverUrl", geoserverUrl);
        ReflectionTestUtils.setField(rs, "geoserverUsername", geoserverUsername);
        ReflectionTestUtils.setField(rs, "geoserverPassword", geoserverPassword);

        rs.sendFile(up);

        try {
            assertTrue(TicketGenerator.isClosed(ticket));
        } catch (GeoServerException e) {
        }
    }

}
