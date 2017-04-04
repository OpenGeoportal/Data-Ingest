package org.opengeoportal.dataingest.utils;

import java.util.List;
import java.util.Map;

/**
 * The Class DatasetsPageWrapper.
 */
public class DatasetsPageWrapper {

    /** The data. */
    private List<Map<String, String>> data;

    /** The showing elements. */
    private int showingElements;

    /** The total elements. */
    private int totalElements;

    /** The curret page. */
    private int currentPage;

    /** The total pages. */
    private int totalPages;

    /**
     * Instantiates a new datasets page wrapper.
     *
     * @param data
     *            the data
     * @param totalElements
     *            the total elements
     * @param currentPage
     *            the current page
     * @param totalPages
     *            the total pages
     */
    public DatasetsPageWrapper(final List<Map<String, String>> data,
            final int totalElements, final int currentPage,
            final int totalPages) {
        this.data = data;
        this.showingElements = data.size();
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public final List<Map<String, String>> getData() {
        return data;
    }

    /**
     * Sets the data.
     *
     * @param data
     *            the data
     */
    public final void setData(final List<Map<String, String>> data) {
        this.data = data;
    }

    /**
     * Gets the total elements.
     *
     * @return the total elements
     */
    public final int getTotalElements() {
        return totalElements;
    }

    /**
     * Sets the total elements.
     *
     * @param totalElements
     *            the new total elements
     */
    public final void setTotalElements(final int totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * Gets the curret page.
     *
     * @return the curret page
     */
    public final int getcurrentPage() {
        return currentPage;
    }

    /**
     * Sets the current page.
     *
     * @param aPage the new current page
     */
    public final void setcurrentPage(final int aPage) {
        this.currentPage = aPage;
    }

    /**
     * Gets the total pages.
     *
     * @return the total pages
     */
    public final int getTotalPages() {
        return totalPages;
    }

    /**
     * Sets the total pages.
     *
     * @param totalPages
     *            the new total pages
     */
    public final void setTotalPages(final int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * Gets the showing elements.
     *
     * @return the showing elements
     */
    public final int getShowingElements() {
        return showingElements;
    }

    /**
     * Sets the showing elements.
     *
     * @param showingElements
     *            the new showing elements
     */
    public final void setShowingElements(final int showingElements) {
        this.showingElements = showingElements;
    }

}
