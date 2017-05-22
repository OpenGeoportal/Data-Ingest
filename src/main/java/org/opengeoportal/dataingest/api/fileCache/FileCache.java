package org.opengeoportal.dataingest.api.fileCache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.opengeoportal.dataingest.api.download.DownloadRequest;
import org.opengeoportal.dataingest.api.download.DownloadWrapper;
import org.opengeoportal.dataingest.api.download.WFSClient;
import org.opengeoportal.dataingest.exception.CacheCapacityException;
import org.opengeoportal.dataingest.exception.FileNotReadyException;
import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.opengeoportal.dataingest.utils.GeoServerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by joana on 22/02/17.
 */
@Component
public abstract class FileCache implements Serializable {
    /**
     * Spring boot logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * File Cache structure, which holds the nodes.
     */
    protected HashMap<String, Node> map = new HashMap<String, Node>();

    /**
     * Solr url (from the application.properties).
     */
    @Value("${localSolr.url}")
    private String localSolrUrl;

    /**
     * Max allowable lock time in seconds 1 hour is recommended for bigger
     * downloads.
     */
    @Value("${file.maxAllowableLockTime}")
    private long maxAllowableLockTime;
    /**
     * Cache directory maximum size (in bytes).
     */
    @Value("${cache.capacity}")
    private Long capacity;
    /**
     * Disk path of the file cache (where we store the physical files).
     */
    @Value("${cache.path}")
    private String path;
    /**
     * Name of the cache, which is the directory to be created on given path.
     */
    @Value("${cache.name}")
    private String cachename;
    /**
     * Validity of the cache (in seconds).
     */
    @Value("${param.download.max.age.file}")
    private Long maxDownloadFileAgeInSeconds;
    /**
     * The GeoServer URL (from the application.properties).
     */
    @Value("${geoserver.url}")
    private String geoserverUrl;
    /**
     * The application context.
     */
    @Autowired
    private ApplicationContext context;

    /**
     * Init method, that creates the cache directory.
     *
     * @throws Exception the exception
     */
    @PostConstruct
    public void createCacheDir() throws Exception {
        // Make sure the path is never empty here
        path = (path == null || path.isEmpty() ? System.getProperty(
            "java.io.tmpdir") : path);
        final String baseDir = FileNameUtils.getCachePath(path, cachename);
        try {
            // First lets check if the root directory is ok
            final File root = new File(path);
            if (!root.exists()) {
                throw new FileNotFoundException();
            }
            if (!root.canRead() || !root.canWrite()) {
                throw new java.lang.SecurityException();
            }
            // Now lets check the complete cache path; does it exist? is it
            // acessible?
            final File dir = new File(baseDir);
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    throw new Exception();
                }
            } else {
                if (!dir.isDirectory()) {
                    throw new Exception(baseDir + " is a file, not a folder!");
                }
                if (!dir.canRead() || !dir.canWrite()) {
                    throw new java.lang.SecurityException();
                }
            }
            log.info("Init cache directory on " + baseDir
                + ", with a capacity of "
                + (double) capacity / (1024.0 * 1024.0) + " MB");
        } catch (final FileNotFoundException fe) {
            throw new Exception("Cache root path " + path
                + " does not exist or is invalid");
        } catch (final java.lang.SecurityException se) {
            throw new Exception("Could not init cache folder at " + baseDir
                + "; please check permissions");
        }
    }

    /**
     * Wrapper of the createCacheDir function, which takes the capacity, cachename and path as arguments.
     *
     * @param aCapacity  cache capacity.
     * @param aCachename cache name.
     * @param aPath      cache path on disk.
     * @throws Exception
     */
    public void createCacheDir(long aCapacity, String aCachename, String aPath) throws Exception {
        capacity = aCapacity;
        cachename = aCachename;
        path = aPath;

        createCacheDir();
    }

    /**
     * Shutdown method which removes the cache directory, after the JVM is
     * stopped.
     */
    @PreDestroy
    public void clearCacheDir() throws IOException {
        log.info("Cleaning up: removing cache folder at "
            + FileNameUtils.getCachePath(path, cachename));
        final File dir = new File(FileNameUtils.getCachePath(path, cachename));
        FileUtils.deleteDirectory(dir);
    }

    /**
     * Public interface for the abstract remove, taking a typename as argument.
     *
     * @param key dataset typename (workspace:dataset)
     * @throws Exception the exception
     */
    public void remove(final String key) throws Exception {
        final Node n = map.get(key);
        if (n == null) {
            throw new Exception("No cache register found for " + key);
        }
        remove(n);
    }

    /**
     * This is the main method to interact with the file cache. We ask for a
     * file and it searches in the cache. If its there, retrieve it from the
     * disk, otherwise issue an assynchronous download and register the file on
     * the cache.
     *
     * @param key dataset typename (workspace:dataset)
     * @return file
     * @throws Exception the exception
     */
    public File getFileFromCache(final String key) throws java.lang.Exception, CacheCapacityException,
        FileNotReadyException {

        final File f = null;
        FileManager fileM;
        final String workspace = GeoServerUtils.getWorkspace(key);
        final String dataset = GeoServerUtils.getDataset(key);
        final String fileName = FileNameUtils.getFullPathZipFile(
            FileNameUtils.getCachePath(path, cachename), workspace,
            dataset);

        final Node n = map.get(key);

        final WFSClient client = new WFSClient();
        final long fileSize = client.getFileSize(geoserverUrl, workspace, dataset);

        try {

            // File is cached
            if (get(key) != null) {

                // The file size is different from last time
                if (fileSize != n.getValue()) {
                    try {
                        n.setValue(fileSize);
                    } catch (final Exception ex) {
                        throw new Exception(
                            "Could not register file on the cache");
                    }
                    throw new FileNotReadyException();
                }

                fileM = new FileManager(fileName, maxAllowableLockTime);
                // Check for age, for oldest files
                if (fileM.getFileAgeinSeconds() >= maxDownloadFileAgeInSeconds) {
                    downloadFileFromRemote(workspace, dataset);
                    throw new FileNotReadyException();
                } else {
                    DownloadWrapper dM= new DownloadWrapper();
                    return dM.getFile(workspace, dataset, localSolrUrl, geoserverUrl, fileName);
                }

            } else { // File is not cached

                set(key, fileSize);
                throw new FileNotReadyException();

            }
        } catch (final FileNotReadyException fnrex) {
            downloadFileFromRemote(workspace, dataset);
            throw fnrex;
        }
    }

    /**
     * Utility method to check if a file is cached.
     *
     * @param key dataset typename (workspace:dataset)
     * @return boolean to indicate if its cached (true) or not (false)
     * @throws Exception the exception
     */
    public boolean isCached(final String key) throws Exception {
        return get(key) != null;
    }

    /**
     * Utility method to get current size of the cache directory.
     *
     * @return size in bytes
     */
    protected Long getDiskSize() {
        long diskSize = 0;
        for (final Node value : map.values()) {
            diskSize += value.getValue();
        }
        return diskSize;
    }

    /**
     * Utility method to remove a file from disk.
     *
     * @param key dataset typename (workspace:dataset)
     * @throws Exception the exception
     */
    protected void removeFile(final String key) throws Exception {
        final String workspace = GeoServerUtils.getWorkspace(key);
        final String dataset = GeoServerUtils.getDataset(key);
        final String fileName = FileNameUtils.getFullPathZipFile(
            FileNameUtils.getCachePath(path, cachename), workspace,
            dataset);

        try {
            final FileManager fileM = new FileManager(fileName, maxAllowableLockTime);
            fileM.removeFile();

        } catch (final IOException e) {
            throw new Exception("Could not remove " + fileName);
        } catch (final Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * Abstract method to retrieve a node from the cache.
     *
     * @param key dataset typename (workspace:dataset)
     * @return a node or null, if it doesnt find the typename
     * @throws Exception the exception
     */
    protected abstract Node get(String key) throws Exception;

    /**
     * Abstract method to put a file in the cache.
     *
     * @param key   dataset typename (workspace:dataset)
     * @param value file size
     * @throws Exception the exception
     */
    protected abstract void set(String key, long value) throws Exception;

    /**
     * Method to remove a file from the cache. - remove it from the disk -
     * unregister it from the cache structure
     *
     * @param n node (typename,size)
     * @throws Exception the exception
     */
    protected abstract void remove(Node n) throws Exception;

    /**
     * Gets the file from remote.
     *
     * @param workspace the workspace
     * @param dataset   the dataset
     */
    private void downloadFileFromRemote(final String workspace,
                                        final String dataset) {
        final JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        jmsTemplate.convertAndSend("fileRequestsQueue",
            new DownloadRequest(workspace, dataset));

    }

    /**
     * Searches for a physical file on the disk cache, and if it doesn't find
     * it, triggers the asynchronous download.
     *
     * @param n node (typename,size)
     * @return a file
     * @throws Exception the exception
     */
    @Deprecated
    private File getFile(final Node n) throws Exception {

        final String workspace = GeoServerUtils.getWorkspace(n.getKey());
        final String dataset = GeoServerUtils.getDataset(n.getKey());

        final String fileName = FileNameUtils.getFullPathZipFile(
            FileNameUtils.getCachePath(path, cachename), workspace,
            dataset);

        FileManager fileM = null;

        try {
            final WFSClient client = new WFSClient();
            final long fileSize = client.getFileSize(geoserverUrl, workspace, dataset);

            // The file size is different from last time
            if (fileSize != n.getValue()) {
                n.setValue(fileSize);
                throw new FileNotReadyException();
            }
            // The files already exists and is not locked (downloading)
            fileM = new FileManager(fileName, maxAllowableLockTime);
            // Check for age, for oldest files
            if (fileM.getFileAgeinSeconds() >= maxDownloadFileAgeInSeconds) {
                downloadFileFromRemote(workspace, dataset);
                throw new FileNotReadyException();
            } else {
                return fileM.getFile();
            }
        } catch (final FileNotReadyException fnrex) {
            downloadFileFromRemote(workspace, dataset);
            throw fnrex;
        }
    }

    /**
     * Gets the capacity.
     *
     * @return the capacity
     */
    public Long getCapacity() {
        return capacity;
    }

}
