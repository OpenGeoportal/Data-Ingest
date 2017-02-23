package org.opengeoportal.dataingest.api.fileCache;

import org.opengeoportal.dataingest.api.download.DownloadRequest;
import org.opengeoportal.dataingest.api.download.WFSClient;
import org.opengeoportal.dataingest.exception.FileNotReadyException;
import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.opengeoportal.dataingest.utils.GeoServerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import java.io.File;
import java.util.HashMap;

/**
 * Created by joana on 22/02/17.
 */

public abstract class FileCache {

    /**
     * The Constant maxDownloadFileAgeInSeconds.
     */
    private static final long MAX_DOWNLOAD_FILE_AGE_IN_SECONDS = 86400; // 24H
    /**
     * Cache directory maximum size (in bytes).
     */
    private final int capacity;
    /**
     * Disk path of the file cache (where we store the physical files).
     */
    private final String path;
    /**
     * File Cache structure, which holds the nodes.
     */
    protected HashMap<Integer, Node> map = new HashMap<Integer, Node>();
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
     * Class constructor, where we initialize capacity and path.
     *
     * @param capacity cache directory maximum size (in bytes).
     * @param path     disk path of the file cache (where we store the physical
     *                 files.
     */
    public FileCache(final int capacity, final String path) {
        this.capacity = capacity;
        this.path = path;
    }

    /**
     * Wrapper of the remove(Node n), function, taking a typename as argument.
     *
     * @param key dataset typename (workspace:dataset)
     */
    public void remove(final String key) {
        remove(map.get(key));
    }

    /**
     * This is the main method to interact with the file cache.
     * We ask for a file and it searches in the cache. If its there, retrieve it from the disk, otherwise
     * issue an assynchronous download and register the file on the cache.
     *
     * @param key dataset typename (workspace:dataset)
     * @return file
     */
    public File getFileFromCache(String key) throws Exception {

        File f = null;
        FileManager fileM;
        final String workspace = GeoServerUtils.getWorkspace(key);
        final String dataset = GeoServerUtils.getDataset(key);
        final String fileName = FileNameUtils.getFullPathZipFile(workspace,
            dataset);
        final String uri = geoserverUrl + workspace
            + "/ows?service=WFS&version=1.0.0&request=GetFeature&typeName="
            + workspace + ":" + dataset + "&outputFormat=SHAPE-ZIP";
        Node n = map.get(key);

        final WFSClient client = new WFSClient();
        final long fileSize = client.getFileSize(uri, fileName);

        try {

            // File is ached
            if (get(key) != null) {

                // The file size is different from last time
                if (fileSize != n.getValue()) {
                    n.setValue(fileSize);
                    throw new FileNotReadyException();
                }

                fileM = new FileManager(fileName);
                // Check for age, for oldest files
                if (fileM.getFileAgeinSeconds() > MAX_DOWNLOAD_FILE_AGE_IN_SECONDS) {
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
     * @param key dataset typename (workspace:dataset)
     * @return boolean to indicate if its cached (true) or not (false)
     */
    public boolean isCached(String key) {
        return get(key) != null;
    }

    /**
     * Abstract method to retrieve a node from the cache.
     *
     * @param key dataset typename (workspace:dataset)
     * @return a node or null, if it doesnt find the typename
     */
    protected abstract Node get(String key);

    /**
     * Abstract method to put a file in the cache.
     *
     * @param key   dataset typename (workspace:dataset)
     * @param value file size
     */
    protected abstract void set(String key, long value);

    /**
     * method to remove a file from the cache.
     *
     * @param n node (typename,size)
     */
    private void remove(Node n) {
        //TODO: throw an error here
        map.remove(n);
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
            if (fileM.getFileAgeinSeconds() > MAX_DOWNLOAD_FILE_AGE_IN_SECONDS) {
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
     * Gets the file from remote.
     *
     * @param workspace the workspace
     * @param dataset   the dataset
     */
    private void getFileFromRemote(final String workspace,
                                   final String dataset) {
        final JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        jmsTemplate.convertAndSend("fileRequestsQueue",
            new DownloadRequest(workspace, dataset));

    }

}
