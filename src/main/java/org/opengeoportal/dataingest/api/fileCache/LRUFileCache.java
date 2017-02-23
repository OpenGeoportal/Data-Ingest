package org.opengeoportal.dataingest.api.fileCache;

import java.io.File;

/**
 * Created by joana on 22/02/17.
 */
public class LRUFileCache extends FileCache {

    /**
     * Calls the abstract class constructor.
     *
     * @param capacity cache directory maximum size (in bytes).
     * @param path     disk path of the file cache (where we store the physical files.
     */
    public LRUFileCache(int capacity, String path) {
        super(capacity, path);
    }

    /**
     * Overriden method from FileCache, which puts an entry in the LRU cache.
     *
     * @param key dataset typename (workspace:dataset)
     * @return file
     */
    public File get(String key) {
        //TODO: implement this
        //getFile
        return null;
    }

    /**
     * Overriden method from the FileCache, which retrieves an entry form the LRU cache.
     *
     * @param key dataset typename (workspace:dataset)
     */
    public void set(String key) {
        //TODO: implement this
        //setFile
    }

    /**
     * Overriden method from the file cache, which removes an entry from the LRU cache.
     *
     * @param n node (typename,size)
     */
    protected void remove(Node n) {
        //TODO: throw an error here
        map.remove(n);
    }

}
