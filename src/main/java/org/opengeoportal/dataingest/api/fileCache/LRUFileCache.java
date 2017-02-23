package org.opengeoportal.dataingest.api.fileCache;

import java.io.File;

/**
 * Created by joana on 22/02/17.
 */
public class LRUFileCache extends FileCache {

    /**
     * Calls the abstract class constructor.
     *
     * @param capacity
     *            cache directory maximum size (in bytes).
     * @param path
     *            disk path of the file cache (where we store the physical
     *            files.
     */
    public LRUFileCache(final int capacity, final String path) {
        super(capacity, path);
    }

    /**
     * Overriden method from FileCache, which puts an entry in the LRU cache.
     *
     * @param key
     *            dataset typename (workspace:dataset)
     * @return file
     */
    @Override
    public File get(final String key) {
        // TODO: implement this
        // getFile
        return null;
    }

    /**
     * Overriden method from the FileCache, which retrieves an entry form the
     * LRU cache.
     *
     * @param key
     *            dataset typename (workspace:dataset)
     */
    @Override
    public void set(final String key) {
        // TODO: implement this
        // setFile
    }

    /**
     * Overriden method from the file cache, which removes an entry from the LRU
     * cache.
     *
     * @param n
     *            node (typename,size)
     */
    @Override
    protected void remove(final Node n) {
        // TODO: throw an error here
        map.remove(n);
    }

}
