/*
 * @author Antonio
 */
package org.opengeoportal.dataingest.api.fileCache;

import org.opengeoportal.dataingest.exception.CacheCapacityException;
import org.springframework.stereotype.Component;

/**
 * Created by joana on 22/02/17.
 */
@Component
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
     * Overriden method from FileCache, which puts an entry in the LRU cache.
     *
     * @param key dataset typename (workspace:dataset)
     * @return file
     * @throws Exception the exception
     */
    @Override
    protected Node get(final String key) throws Exception {

        if (map.containsKey(key)) {
            final Node n = map.get(key);
            if (n == null) {
                throw new Exception(
                    "Could not find a register on the cache for " + key);
            }
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
     * @throws Exception the exception
     */
    @Override
    protected void set(final String key, final long value) throws CacheCapacityException, java.lang.Exception {
        if (map.containsKey(key)) {
            final Node old = map.get(key);
            old.setValue(value);
            removeNode(old);
            setHead(old);
        } else {
            if (value >= this.getCapacity()) throw new CacheCapacityException(" File " + key + " (" + value + " "
                + " bytes) exceeds the capacity of the file cache (" + this.getCapacity() + " bytes). Please review "
                + "your cache configuration.");
            final Node created = new Node(key, value);
            // Here we check of the cache has reached its capacity, and perform
            // accordingly.
            if ((getDiskSize() + value) >= this.getCapacity()) {
                remove(end);
                setHead(created);
            } else {
                setHead(created);
            }

            map.put(key, created);
        }
    }

    /**
     * Overriden method from the FileCache, which evicts the file cache. -
     * remove physical file - remove cache entry
     *
     * @param n node (typename,size)
     * @throws Exception the exception
     */
    @Override
    protected void remove(final Node n) throws Exception {
        // First remove file, so if something fails we don't unregister it from the cache
        removeFile(n.getKey());
        map.remove(n.getKey());
        removeNode(n);
    }

    /**
     * Remove node and adjust contiguous nodes.
     *
     * @param n node (typename,size)
     */
    private void removeNode(final Node n) {
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
    private void setHead(final Node n) {
        n.setNext(head);
        n.setPre(null);

        if (head != null) {
            head.setPre(n);
        }
        head = n;
        if (end == null) {
            end = head;
        }
    }

}
