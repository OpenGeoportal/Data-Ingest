/*
 * @author Antonio
 */
package org.opengeoportal.dataingest.api.download;

import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import org.opengeoportal.dataingest.api.fileCache.LRUFileCache;
import org.opengeoportal.dataingest.exception.CacheCapacityException;
import org.opengeoportal.dataingest.exception.FileNotReadyException;
import org.opengeoportal.dataingest.utils.GeoServerUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * The Class LocalDownloadService.
 */
@Component
public class LocalDownloadService {

    /**
     * A file cache, following the LRU eviction policy.
     */
    private LRUFileCache fileCache;

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
     * Get the file cache of the local download.
     *
     * @return file cache
     */
    public LRUFileCache getFileCache() {
        return fileCache;
    }

    /**
     * Constructor.
     *
     * @param fileCache the new file cache
     */
    public void setFileCache(final LRUFileCache fileCache) {
        this.fileCache = fileCache;
    }

    /**
     * Check if data exists on Geoserver.
     *
     * @param workspace the workspace
     * @param dataset   the dataset
     * @return true
     * @throws Exception the exception
     */
    private boolean isAValidRequest(final String workspace,
                                    final String dataset) throws Exception {

        try {

            final GeoServerRESTReader reader = new GeoServerRESTReader(geoserverUrl,
                geoserverUsername, geoserverPassword);

            return reader.existsLayer(workspace, dataset, true);

        } catch (final java.io.IOException ex) {

            final String typeName = GeoServerUtils.getTypeName(workspace,
                dataset);
            if (fileCache.isCached(typeName)) {
                fileCache.remove(typeName);
            }

            return false;
        }

    }

    /**
     * This is the method that we use for getting the file. It uses the
     * FileCache.
     *
     * @param workspace the workspace
     * @param dataset   the dataset
     * @return The requested file
     * @throws FileNotReadyException File not ready locally
     * @throws IOException           Signals that an I/O exception has occurred.
     * @throws Exception             the exception
     */
    public final File getFile(final String workspace, final String dataset)
        throws FileNotReadyException, IOException, java.lang.Exception, CacheCapacityException {

        // this data exists
        if (!this.isAValidRequest(workspace, dataset)) {
            throw new FileNotFoundException();
        }

        final String typeName = GeoServerUtils.getTypeName(workspace, dataset);
        return fileCache.getFileFromCache(typeName);
    }

    /**
     * This is the method force the download of the file, ignoring the cache .
     *
     * @param workspace the workspace
     * @param dataset   the dataset
     * @param fileName  filename
     * @throws FileNotFoundException File not found on OGS
     */
    @Deprecated // This won't work anymore! Move it to the FileCache
    public final void forceGetFile(final String workspace, final String dataset,
                                   final String fileName) throws FileNotFoundException {
        // this.getFileFromRemote(workspace, dataset);
    }

}
