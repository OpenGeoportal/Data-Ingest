package org.opengeoportal.dataingest.api.fileCache;

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
    public Node get(String key) {
        //TODO: implement this
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
    protected void set(String key, long value) {
        //TODO: implement this
        //setFile
    }

}
