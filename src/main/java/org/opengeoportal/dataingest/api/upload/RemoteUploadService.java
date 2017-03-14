package org.opengeoportal.dataingest.api.upload;

import org.opengeoportal.dataingest.utils.GeoServerRESTFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * The Class RemoteUploadService. This class is used to send a shapefile to GS.
 */
@Component
public class RemoteUploadService {

    /**
     * The GeoServer URL (from the application.properties).
     */
    @Value("${geoserver.url}")
    private String geoserverUrl;

    /**
     * The GeoServer Username (from the application.properties).
     */
    @Value("${geoserver.username}")
    private String geoserverUsername;

    /**
     * The GeoServer Password (from the application.properties).
     */
    @Value("${geoserver.password}")
    private String geoserverPassword;

    /**
     * Send file.
     *
     * @param uploadRequest
     *            the upload request
     * @throws Exception
     *             the exception
     */
    public final void sendFile(final UploadRequest uploadRequest, final String strEpsg)
            throws Exception {

        final String workspace = uploadRequest.getWorkspace();
        final String dataset = uploadRequest.getDataset();
        final File zipFile = uploadRequest.getZipFile();

        final GeoServerRESTFacade geoServerFacade = new GeoServerRESTFacade(
                geoserverUrl, geoserverUsername, geoserverPassword);

        geoServerFacade.publishShp(workspace, dataset, dataset, zipFile,
            strEpsg);

    }

}
