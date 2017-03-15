package org.opengeoportal.dataingest.api.upload;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.opengeoportal.dataingest.exception.GeoServerException;
import org.opengeoportal.dataingest.utils.ShapeFileValidator;
import org.opengeoportal.dataingest.utils.TicketGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

public class UploadTest {

    private static String AIRPORTS_SHAPEFILE = "./src/test/resources/airports.zip";
    private String geoserverUrl = "http://localhost:8081/geoserver/";
    private String geoserverUsername = "admin";
    private String geoserverPassword = "geoserver";

    @Test
    public void testShapeFile() throws Exception {

        File shpf = new File(AIRPORTS_SHAPEFILE);
        boolean noException = false;
        
        try {
            assertEquals(ShapeFileValidator.isAValidShapeFile(shpf, true), "EPSG:2964");
            noException = true;
        } catch (Exception e) {
            assertTrue(noException);
        }

    }
    
    @Test
    public void testUpload() throws Exception {
        File shpf = new File(AIRPORTS_SHAPEFILE);
        
        long ticket = TicketGenerator.openATicket();
        
        UploadRequest up = new UploadRequest("topp", "airports", shpf, "strEpsg", ticket, false);
        
        RemoteUploadService rs = new RemoteUploadService();
        
        ReflectionTestUtils.setField(rs, "geoserverUrl", geoserverUrl);
        ReflectionTestUtils.setField(rs, "geoserverUsername", geoserverUsername);
        ReflectionTestUtils.setField(rs, "geoserverPassword", geoserverPassword);
        
        rs.sendFile(up);
        
        try {
            assertTrue(TicketGenerator.isClosed(ticket));
        } catch(GeoServerException e) {}
    }

}
