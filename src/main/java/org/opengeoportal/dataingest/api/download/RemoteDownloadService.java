package org.opengeoportal.dataingest.api.download;

import java.io.File;

import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The Class RemoteDownloadService.
 * This class is used to organize the download with GeoServer.
 */
@Component
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

        String workspace = downloadRequest.getWorkspace();
        String dataset = downloadRequest.getDataset();

        String uri = geoserverUrl + "/" + workspace
            + "/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=" + workspace + ":" + dataset
            + "&outputFormat=SHAPE-ZIP";

        WFSClient client = new WFSClient();

        String fileName = FileNameUtils.getFullPathZipFile(workspace, dataset);
        File file = client.getFile(uri, fileName);

    }

}
