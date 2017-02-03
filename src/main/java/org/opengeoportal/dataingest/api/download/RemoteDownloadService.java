package org.opengeoportal.dataingest.api.download;

import java.io.File;

import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 * The Class RemoteDownloadService.
 * This class is used to organize the download with GeoServer.
 */
public class RemoteDownloadService {

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
     * Prepare download.
     *
     * @param downloadRequest the download request
     * @throws Exception the exception
     */
    public final void prepareDownload(final DownloadRequest downloadRequest) throws Exception {

        final String workspace = downloadRequest.getWorkspace();
        final String dataset = downloadRequest.getDataset();

        final String uri = geoserverUrl + "/" + workspace
            + "/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=" + workspace + ":" + dataset
            + "&outputFormat=SHAPE-ZIP";

        final WFSClient client = new WFSClient();

        final String fileName = FileNameUtils.getFullPathZipFile(workspace, dataset);
        final File file = client.getFile(uri, fileName);

    }

}
