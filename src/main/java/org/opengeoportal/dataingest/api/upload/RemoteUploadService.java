package org.opengeoportal.dataingest.api.upload;

import org.opengeoportal.dataingest.utils.GeoServerRESTFacade;
import org.opengeoportal.dataingest.utils.TicketGenerator;
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
    public final void sendFile(final UploadRequest uploadRequest)
            throws Exception {

        final String workspace = uploadRequest.getWorkspace();
        final String dataset = uploadRequest.getDataset();
        final String store = uploadRequest.getStore();
        final File zipFile = uploadRequest.getZipFile();
        final String strEpsg = uploadRequest.getStrEpsg();
        final boolean isUpdate = uploadRequest.isUpdate();

        final GeoServerRESTFacade geoServerFacade = new GeoServerRESTFacade(
                geoserverUrl, geoserverUsername, geoserverPassword);

        try {

            if (isUpdate) {
                if (geoServerFacade.republishShp(workspace, store, dataset, zipFile, strEpsg)) {
                    TicketGenerator.closeATicket(uploadRequest.getTicket());
                } else {
                    TicketGenerator.closeATicket(uploadRequest.getTicket(), "Generic bad response");
                }
            } else {
                if (geoServerFacade.publishShp(workspace, store, dataset, zipFile, strEpsg)) {
                    TicketGenerator.closeATicket(uploadRequest.getTicket());
                } else {
                    TicketGenerator.closeATicket(uploadRequest.getTicket(), "Generic bad response");
                }
            }

        } catch (final Exception ex) {
            TicketGenerator.closeATicket(uploadRequest.getTicket(), ex.getMessage());
        }

    }

}
