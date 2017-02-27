package org.opengeoportal.dataingest.api.download;

import org.opengeoportal.dataingest.api.fileCache.FileManager;
import org.opengeoportal.dataingest.exception.FileNotReadyException;
import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * The Class RemoteDownloadService. This class is used to organize the download
 * with GeoServer.
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
     * Max age allowed for files in cache.
     */
    @Value("${param.download.max.age.file}")
    private long maxDownloadFileAgeInSeconds;
    /**
     * Name of the cache, which is the directory to be created on given path.
     */
    @Value("${cache.name}")
    private String cachename;
    /**
     * Disk path of the file cache (where we store the physical files).
     */
    @Value("${cache.path}")
    private String path;

    /**
     * Prepare download.
     *
     * @param downloadRequest the download request
     * @return the file
     * @throws Exception the exception
     */
    public final File prepareDownload(final DownloadRequest downloadRequest)
        throws Exception {

        final String workspace = downloadRequest.getWorkspace();
        final String dataset = downloadRequest.getDataset();

        final String uri = geoserverUrl + workspace
            + "/ows?service=WFS&version=1.0.0&request=GetFeature&typeName="
            + workspace + ":" + dataset + "&outputFormat=SHAPE-ZIP";

        final WFSClient client = new WFSClient();

        final String fileName = FileNameUtils.getFullPathZipFile(FileNameUtils.getCachePath(path, cachename), workspace,
            dataset);

        FileManager fileM = null;
        try {
            fileM = new FileManager(fileName);
            if (fileM.getFileAgeinSeconds() <= maxDownloadFileAgeInSeconds) {
                return fileM.getFile();
            }
        } catch (final FileNotReadyException fnrex) {
            // ok
        }


        final File file = client.getFile(uri, fileName);
        return file;

    }

}
