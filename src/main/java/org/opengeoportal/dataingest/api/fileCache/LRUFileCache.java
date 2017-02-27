package org.opengeoportal.dataingest.api.fileCache;

/**
 * Created by joana on 22/02/17.
 */
public class LRUFileCache extends FileCache {

    /**
     * Head of the list of double linked nodes.
     */
    private Node head = null;
    /**
     * Tail of the list of double linked nodes.
     */
    private Node end = null;

    /**
     * Calls the abstract class constructor.
     *
     * @param capacity cache directory maximum size (in bytes).
     * @param path     disk path of the file cache (where we store the physical
     *                 files.
     */
    public LRUFileCache(final int capacity, final String path, long age, String geoserverUrl) {
        super(capacity, path, age, geoserverUrl);
    }

    /**
     * Overriden method from FileCache, which puts an entry in the LRU cache.
     *
     * @param key dataset typename (workspace:dataset)
     * @return file
     */
    @Override
    protected Node get(String key) throws Exception {

        if (map.containsKey(key)) {
            Node n = map.get(key);
            if (n == null) throw new Exception("Could not find a register on the cache for " + key);
            removeNode(n);
            setHead(n);
            return n;
        }

        return null;
    }

    /**
     * Overriden method from the FileCache, which retrieves an entry form the
     * LRU cache.
     *
     * @param key   dataset typename (workspace:dataset)
     * @param value file size
     */
    @Override
    protected void set(String key, long value) throws Exception {
        if (map.containsKey(key)) {
            Node old = map.get(key);
            old.setValue(value);
            removeNode(old);
            setHead(old);
        } else {
            Node created = new Node(key, value);
            // Here we check of the cache has reached its capacity, and perform accordingly.
            if (getDiskSize() >= capacity) {
                remove(end);
                setHead(created);
            } else {
                setHead(created);
            }

            map.put(key, created);
        }
    }

    /**
     * Overriden method from the FileCache, which evicts the file cache.
     * - remove physical file
     * - remove cache entry
     *
     * @param n node (typename,size)
     * @throws Exception
     */
    @Override
    protected void remove(Node n) throws Exception {
        // First remove file, so if something fails we don't unregister it from the
        // cache
        removeFile(n.getKey());
        map.remove(n.getKey());
        removeNode(n);
    }


    /**
     * Remove node and adjust contiguous nodes.
     *
     * @param n node (typename,size)
     */
    private void removeNode(Node n) {
        if (n.getPre() != null) {
            n.getPre().setNext(n.getNext());
        } else {
            head = n.getNext();
        }

        if (n.getNext() != null) {
            n.getNext().setPre(n.getPre());
        } else {
            end = n.getPre();
        }

    }

    /**
     * Set head of the double linked node list.
     *
     * @param n node (typename,size)
     */
    private void setHead(Node n) {
        n.setNext(head);
        n.setPre(null);

        if (head != null) head.setPre(n);
        head = n;
        if (end == null) end = head;
    }

}
