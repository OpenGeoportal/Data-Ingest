package org.opengeoportal.dataingest.utils;

import java.util.List;
import java.util.Map;

/**
 * The Class ResultSortedPaginator.
 */
public class ResultSortedPaginator {

    /**
     * the list over which this class is paging.
     */
    private final List<Map<String, String>> dataListFormat;

    /**
     * the page size.
     */
    private int pageSize;

    /**
     * the current page.
     */
    private int currentPage;

    /**
     * the starting index.
     */
    private int startingIndex;

    /**
     * the ending index.
     */
    private int endingIndex;

    /**
     * the maximum number of pages.
     */
    private int maxPages;

    /**
     * Instantiates a new result sorted paginator.
     *
     * @param list     the datasets list
     * @param pageSize page size
     * @param sort     defines sorting
     */
    public ResultSortedPaginator(final List<Map<String, String>> list,
                                 final int pageSize, final boolean sort) {

        dataListFormat = list;

        this.pageSize = pageSize;
        this.currentPage = 1;
        this.maxPages = 1;

        calculatePages();
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
    public final List<Map<String, String>> getList() {
        return this.dataListFormat;
    }

    /**
     * Gets the subset of the list for the current page.
     *
     * @return a List
     */
    public final List<Map<String, String>> getHashMapForPage() {
        return this.dataListFormat.subList(startingIndex, endingIndex);
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
     * @param pageSize the page size as an int
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
     * @param p the page as an int
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


}
