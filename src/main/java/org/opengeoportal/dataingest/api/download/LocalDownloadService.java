package org.opengeoportal.dataingest.api.download;

import org.opengeoportal.dataingest.api.GeoserverDataStore;
import org.opengeoportal.dataingest.api.fileCache.LRUFileCache;
import org.opengeoportal.dataingest.exception.FileNotReadyException;
import org.opengeoportal.dataingest.utils.GeoServerUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static LRUFileCache fileCache;
    /**
     * The GeoServer URL (from the application.properties).
     */
    private String geoserverUrl;

    /**
     * Constructor of the Local Download Service, where we initialize a File
     * Cache structure. If no one set he cache path on application.properties,
     * we use the default TMP dir.
     *
     * @param capacity                    cache directory maximum size (in bytes).
     * @param path                        disk path of the file cache (where we store the physical files.
     * @param maxDownloadFileAgeInSeconds validity of the cache (in seconds).
     * @param geoserverUrl                geoserver url.
     * @throws java.lang.NullPointerException
     */
    @Autowired
    public LocalDownloadService(@Value("${cache.capacity}") int capacity, @Value("${cache.path}") String
        path,
                                @Value("${param.download.max.age.file}") long maxDownloadFileAgeInSeconds, @Value
                                    ("${geoserver.url}") String geoserverUrl)
        throws java.lang.NullPointerException {
        this.geoserverUrl = geoserverUrl;
        fileCache = new LRUFileCache(capacity, path == null || path.isEmpty()
            ? System.getProperty("java.io" + ".tmpdir") : path, maxDownloadFileAgeInSeconds, geoserverUrl);
    }

    /**
     * Get the file cache of the local download.
     *
     * @return file cache
     */
    public LRUFileCache getFileCache() {
        return fileCache;
    }

    /**
     * Check if data exists on Geoserver.
     *
     * @param workspace the workspace
     * @param dataset   the dataset
     * @return true
     */
    private boolean isAValidRequest(final String workspace,
                                    final String dataset) {

        /*
         * Find a better way than asking directly for the file (some method in
         * org.geotools.data.store.ContentDataStore?) Maybe Cache this later?
         */

        try {
            final GeoserverDataStore gds = new GeoserverDataStore(geoserverUrl);

            final boolean exists = gds.getLayerInfo(workspace, dataset)
                .size() > 0;

            /**
             * Remove file on the cache, if the request fails *and* the file is
             * on the cache: It means it should *not* be there!
             */
            if (!exists && fileCache.isCached(
                GeoServerUtils.getTypeName(workspace, dataset))) {
                fileCache
                    .remove(GeoServerUtils.getTypeName(workspace, dataset));
            }
            return exists;

        } catch (final Exception ex) {
            ex.printStackTrace();
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
     */
    public final File getFile(final String workspace, final String dataset)
        throws FileNotReadyException, IOException, java.lang.Exception {

        // this data exists
        if (!this.isAValidRequest(workspace, dataset)) {
            throw new FileNotFoundException();
        }


        String typeName = GeoServerUtils.getTypeName(workspace, dataset);
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
