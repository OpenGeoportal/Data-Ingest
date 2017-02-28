/*
 * @author Antonio
 */
package org.opengeoportal.dataingest.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class ResultSortedPaginator.
 */
public class ResultSortedPaginator {

    /** the list over which this class is paging. */
    private final List<HashMapElement> dataListFormat;

    /** the page size. */
    private int pageSize;

    /** the current page. */
    private int currentPage;

    /** the starting index. */
    private int startingIndex;

    /** the ending index. */
    private int endingIndex;

    /** the maximum number of pages. */
    private int maxPages;

    /**
     * Instantiates a new result sorted paginator.
     *
     * @param map
     *            the map
     * @param pageSize
     *            the page size
     * @param sort
     *            the sort
     */
    public ResultSortedPaginator(final HashMap<String, String> map,
            final int pageSize, final boolean sort) {

        dataListFormat = fromHashMapToList(map);

        orderBy(sort);

        this.pageSize = pageSize;
        this.currentPage = 1;
        this.maxPages = 1;

        calculatePages();
    }

    /**
     * Order by.
     *
     * @param sort
     *            the sort
     */
    private void orderBy(final boolean sort) {
        if (sort) {

            Collections.sort(dataListFormat);

        }
    }

    /**
     * From hash map to list.
     *
     * @param map
     *            the map
     * @return the list
     */
    private List<HashMapElement> fromHashMapToList(
            final HashMap<String, String> map) {
        final List<HashMapElement> list = new ArrayList<HashMapElement>();

        // map object comes from the cache, in this way we avoid concurrent
        // modifications
        synchronized (map) {
            final Iterator<Map.Entry<String, String>> it = map.entrySet()
                    .iterator();

            while (it.hasNext()) {
                final Map.Entry<String, String> pair = it.next();
                list.add(new HashMapElement(pair.getKey(), pair.getValue()));
            }
        }

        return list;
    }

    /**
     * From list to hash map.
     *
     * @param lista
     *            the lista
     * @return the map
     */
    private Map<String, String> fromListToHashMap(
            final List<HashMapElement> lista) {
        final Map<String, String> map = new HashMap<String, String>();

        for (final HashMapElement hashMapElement : lista) {
            map.put(hashMapElement.getKey(), hashMapElement.getValue());
        }

        return map;
    }

    /**
     * Calculate pages.
     */
    private void calculatePages() {
        if (pageSize > 0) {
            // calculate how many pages there are
            if (dataListFormat.size() % pageSize == 0) {
                maxPages = dataListFormat.size() / pageSize;
            } else {
                maxPages = (dataListFormat.size() / pageSize) + 1;
            }
        }
    }

    /**
     * Gets the list that this instance is paging over.
     *
     * @return a List
     */
    public final HashMap<String, String> getList() {
        return (HashMap<String, String>) fromListToHashMap(this.dataListFormat);
    }

    /**
     * Gets the subset of the list for the current page.
     *
     * @return a List
     */
    public final HashMap<String, String> getHashMapForPage() {
        return (HashMap<String, String>) fromListToHashMap(
                this.dataListFormat.subList(startingIndex, endingIndex));
    }

    /**
     * Gets the page size.
     *
     * @return the page size as an int
     */
    public final int getPageSize() {
        return this.pageSize;
    }

    /**
     * Sets the page size.
     *
     * @param pageSize
     *            the page size as an int
     */
    public final void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
        calculatePages();
    }

    /**
     * Gets the page.
     *
     * @return the page as an int
     */
    public final int getPage() {
        return this.currentPage;
    }

    /**
     * Sets the page size.
     *
     * @param p
     *            the page as an int
     */
    public final void setPage(final int p) {
        if (p >= maxPages) {
            this.currentPage = maxPages;
        } else if (p <= 1) {
            this.currentPage = 1;
        } else {
            this.currentPage = p;
        }

        // now work out where the sub-list should start and end
        startingIndex = pageSize * (currentPage - 1);
        if (startingIndex < 0) {
            startingIndex = 0;
        }
        endingIndex = startingIndex + pageSize;
        if (endingIndex > dataListFormat.size()) {
            endingIndex = dataListFormat.size();
        }
    }

    /**
     * Gets the maximum number of pages.
     *
     * @return the maximum number of pages as an int
     */
    public final int getMaxPages() {
        return this.maxPages;
    }

    /**
     * Determines whether there is a previous page and gets the page number.
     *
     * @return the previous page number, or zero
     */
    public final int getPreviousPage() {
        if (currentPage > 1) {
            return currentPage - 1;
        } else {
            return 0;
        }
    }

    /**
     * Determines whether there is a next page and gets the page number.
     *
     * @return the next page number, or 0
     */
    public final int getNextPage() {
        if (currentPage < maxPages) {
            return currentPage + 1;
        } else {
            return 0;
        }
    }

    /**
     * The Class HashMapElement for representation in List.
     */
    static class HashMapElement implements Comparable<HashMapElement> {

        /** The key. */
        private String key;

        /** The value. */
        private String value;

        /**
         * Instantiates a new hash map element.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         */
        HashMapElement(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Gets the key.
         *
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * Sets the key.
         *
         * @param key
         *            the new key
         */
        public void setKey(final String key) {
            this.key = key;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value.
         *
         * @param value
         *            the new value
         */
        public void setValue(final String value) {
            this.value = value;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(final HashMapElement o) {
            return this.key.compareTo(o.key);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object arg0) {
            if (arg0 == null) {
                return false;
            }
            if (!(arg0 instanceof HashMapElement)) {
                return false;
            }
            return ((HashMapElement) arg0).getKey().equals(this.getKey());
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            assert false : "hashCode not designed";
            return 1;
        }

    }

}
