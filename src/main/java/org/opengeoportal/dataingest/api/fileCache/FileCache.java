package org.opengeoportal.dataingest.api.fileCache;

import org.opengeoportal.dataingest.api.download.DownloadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import java.io.File;
import java.util.HashMap;

/**
 * Created by joana on 22/02/17.
 */

public abstract class FileCache {

    /**
     * Cache directory maximum size (in bytes).
     */
    private int capacity;
    /**
     * Disk path of the file cache (where we store the physical files).
     */
    private String path;
    /**
     * File Cache structure, which holds the nodes.
     */
    protected HashMap<Integer, Node> map = new HashMap<Integer, Node>();

    /**
     * The application context.
     */
    @Autowired
    private ApplicationContext context;

    /**
     * Class constructor, where we initialize capacity and path.
     *
     * @param capacity cache directory maximum size (in bytes).
     * @param path     disk path of the file cache (where we store the physical files.
     */
    public FileCache(int capacity, String path) {
        this.capacity = capacity;
        this.path = path;
    }

    /**
     * Abstract method to put a file in the cache.
     *
     * @param key dataset typename (workspace:dataset)
     */
    public abstract void set(String key);

    /**
     * Abstract method to retrieve a file from the cache.
     *
     * @param key dataset typename (workspace:dataset)
     * @return a file
     */
    public abstract File get(String key);

    /**
     * Abstract method to remove a file from the cache.
     *
     * @param n node (typename,size)
     */
    protected abstract void remove(Node n);

    /**
     * Wrapper of the remove(Node n), function, taking a typename as argument.
     *
     * @param key dataset typename (workspace:dataset)
     */
    public void remove(String key) {
        remove(map.get(key));
    }

    /**
     * Searches for a physical file on the disk cache, and if it doesn't find it, triggers the assynchronous download.
     *
     * @param n node (typename,size)
     * @return a file
     */
    private File getFile(Node n) {
        //TODO: implement this
        /*
                final String fileName = FileNameUtils.getFullPathZipFile(workspace,
            dataset);

                try {
            // The files already exists and is not locked (downloading)
            fileM = new FileManager(fileName);
            if (fileM.getFileAgeinSeconds() > maxDownloadFileAgeInSeconds) {
                getFileFromRemote(workspace, dataset);
                throw new FileNotReadyException();
            } else {
                return fileM.getFile();
            }
        } catch (FileNotReadyException fnrex) {
            // the files doesn't exists (to check: it can be a problem if maxDownloadFileAgeInSeconds is smaller then
             t he download time)
            getFileFromRemote(workspace, dataset);
            throw fnrex;
        }

        //TODO: SET the size on node value
         */
        return null;
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
