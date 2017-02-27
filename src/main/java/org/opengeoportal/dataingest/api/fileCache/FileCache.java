/*
 * @author Antonio
 */
package org.opengeoportal.dataingest.api.fileCache;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.opengeoportal.dataingest.api.download.DownloadRequest;
import org.opengeoportal.dataingest.api.download.WFSClient;
import org.opengeoportal.dataingest.exception.FileNotReadyException;
import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.opengeoportal.dataingest.utils.GeoServerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by joana on 22/02/17.
 */
@Component
public abstract class FileCache {

    /**
     * Cache directory maximum size (in bytes).
     */
    @Value("${cache.capacity}")
    private int capacity;
    /**
     * Disk path of the file cache (where we store the physical files).
     */
    @Value("${cache.path}")
    private String path;
    /**
     * Validity of the cache (in seconds).
     */
    @Value("${param.download.max.age.file}")
    private long maxDownloadFileAgeInSeconds;
    /**
     * The GeoServer URL (from the application.properties).
     */
    @Value("${geoserver.url}")
    private String geoserverUrl;

    /**
     * File Cache structure, which holds the nodes.
     */
    protected HashMap<String, Node> map = new HashMap<String, Node>();

    /**
     * The application context.
     */
    @Autowired
    private ApplicationContext context;

    /**
     * Public interface for the abstract remove, taking a typename as argument.
     *
     * @param key            dataset typename (workspace:dataset)
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
     * @param key            dataset typename (workspace:dataset)
     * @return file
     * @throws Exception the exception
     */
    public File getFileFromCache(final String key) throws Exception {

        final File f = null;
        FileManager fileM;
        final String workspace = GeoServerUtils.getWorkspace(key);
        final String dataset = GeoServerUtils.getDataset(key);
        final String fileName = FileNameUtils.getFullPathZipFile(workspace,
                dataset);
        final String uri = geoserverUrl + workspace
                + "/ows?service=WFS&version=1.0.0&request=GetFeature&typeName="
                + workspace + ":" + dataset + "&outputFormat=SHAPE-ZIP";
        final Node n = map.get(key);

        final WFSClient client = new WFSClient();
        final long fileSize = client.getFileSize(uri, fileName);

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

                fileM = new FileManager(fileName);
                // Check for age, for oldest files
                if (fileM.getFileAgeinSeconds() > maxDownloadFileAgeInSeconds) {
                    getFileFromRemote(workspace, dataset);
                    throw new FileNotReadyException();
                } else {
                    return fileM.getFile();
                }

            } else { // File is not cached

                set(key, fileSize);
                throw new FileNotReadyException();

            }
        } catch (final FileNotReadyException fnrex) {
            getFileFromRemote(workspace, dataset);
            throw fnrex;
        }
    }

    /**
     * Utility method to check if a file is cached.
     *
     * @param key            dataset typename (workspace:dataset)
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
     * @param key            dataset typename (workspace:dataset)
     * @throws Exception the exception
     */
    protected void removeFile(final String key) throws Exception {
        final String workspace = GeoServerUtils.getWorkspace(key);
        final String dataset = GeoServerUtils.getDataset(key);
        final String fileName = FileNameUtils.getFullPathZipFile(workspace,
                dataset);

        try {
            final FileManager fileM = new FileManager(fileName);
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
     * @param key            dataset typename (workspace:dataset)
     * @return a node or null, if it doesnt find the typename
     * @throws Exception the exception
     */
    protected abstract Node get(String key) throws Exception;

    /**
     * Abstract method to put a file in the cache.
     *
     * @param key            dataset typename (workspace:dataset)
     * @param value            file size
     * @throws Exception the exception
     */
    protected abstract void set(String key, long value) throws Exception;

    /**
     * Method to remove a file from the cache. - remove it from the disk -
     * unregister it from the cache structure
     *
     * @param n            node (typename,size)
     * @throws Exception the exception
     */
    protected abstract void remove(Node n) throws Exception;

    /**
     * Gets the file from remote.
     *
     * @param workspace            the workspace
     * @param dataset            the dataset
     */
    private void getFileFromRemote(final String workspace,
            final String dataset) {
        final JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        jmsTemplate.convertAndSend("fileRequestsQueue",
                new DownloadRequest(workspace, dataset));

    }

    /**
     * Searches for a physical file on the disk cache, and if it doesn't find
     * it, triggers the asynchronous download.
     *
     * @param n
     *            node (typename,size)
     * @return a file
     * @throws Exception
     *             the exception
     */
    @Deprecated
    private File getFile(final Node n) throws Exception {

        final String workspace = GeoServerUtils.getWorkspace(n.getKey());
        final String dataset = GeoServerUtils.getDataset(n.getKey());

        final String fileName = FileNameUtils.getFullPathZipFile(workspace,
                dataset);
        final String uri = geoserverUrl + workspace
                + "/ows?service=WFS&version=1.0.0&request=GetFeature&typeName="
                + workspace + ":" + dataset + "&outputFormat=SHAPE-ZIP";

        FileManager fileM = null;

        try {
            final WFSClient client = new WFSClient();
            final long fileSize = client.getFileSize(uri, fileName);

            // The file size is different from last time
            if (fileSize != n.getValue()) {
                n.setValue(fileSize);
                throw new FileNotReadyException();
            }
            // The files already exists and is not locked (downloading)
            fileM = new FileManager(fileName);
            // Check for age, for oldest files
            if (fileM.getFileAgeinSeconds() > maxDownloadFileAgeInSeconds) {
                getFileFromRemote(workspace, dataset);
                throw new FileNotReadyException();
            } else {
                return fileM.getFile();
            }
        } catch (final FileNotReadyException fnrex) {
            getFileFromRemote(workspace, dataset);
            throw fnrex;
        }
    }

    /**
     * @return the capacity
     */
    public int getCapacity() {
        return capacity;
    }

}
