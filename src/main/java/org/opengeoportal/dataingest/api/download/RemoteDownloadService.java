package org.opengeoportal.dataingest.api.download;

import java.io.File;

import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
     * Max allowable lock time in seconds 1 hour is recommended for bigger
     * downloads.
     */
    @Value("${file.maxAllowableLockTime}")
    private long maxAllowableLockTime;

    /**
     * Prepare download.
     *
     * @param downloadRequest
     *            the download request
     * @throws Exception
     *             the exception
     */
    public final void prepareDownload(final DownloadRequest downloadRequest)
            throws Exception {

        final String workspace = downloadRequest.getWorkspace();
        final String dataset = downloadRequest.getDataset();

        final String uri = geoserverUrl + workspace
                + "/ows?service=WFS&version=1.0.0&request=GetFeature&typeName="
                + workspace + ":" + dataset + "&outputFormat=SHAPE-ZIP";

        final WFSClient client = new WFSClient();

        final String cachePath = FileNameUtils.getCachePath(path, cachename);

        // Cache diretory does not exist? no problem, we create it
        final File f = new File(cachePath);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (final SecurityException se) {
                throw new Exception("Could not create " + cachePath
                        + "; please check permissions");
            }
        }

        final String fileName = FileNameUtils.getFullPathZipFile(cachePath,
                dataset);

        client.getFile(uri, fileName, maxAllowableLockTime);

    }

}
