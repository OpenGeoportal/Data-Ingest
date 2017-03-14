package org.opengeoportal.dataingest.utils;

import java.util.HashMap;
import java.util.List;

/**
 * The Class DatasetsPageWrapper.
 */
public class DatasetsPageWrapper {

    /** The data. */
    private HashMap<String, List<String>> data;

    /** The showing elements. */
    private int showingElements;

    /** The total elements. */
    private int totalElements;

    /** The curret page. */
    private int curretPage;

    /** The total pages. */
    private int totalPages;

    /**
     * Instantiates a new datasets page wrapper.
     *
     * @param data
     *            the data
     * @param totalElements
     *            the total elements
     * @param curretPage
     *            the curret page
     * @param totalPages
     *            the total pages
     */
    public DatasetsPageWrapper(final HashMap<String, List<String>> data,
            final int totalElements, final int curretPage,
            final int totalPages) {
        this.data = data;
        this.showingElements = data.size();
        this.totalElements = totalElements;
        this.curretPage = curretPage;
        this.totalPages = totalPages;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public final HashMap<String, List<String>> getData() {
        return data;
    }

    /**
     * Sets the data.
     *
     * @param data
     *            the data
     */
    public final void setData(final HashMap<String, List<String>> data) {
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
    public final int getCurretPage() {
        return curretPage;
    }

    /**
     * Sets the curret page.
     *
     * @param curretPage
     *            the new curret page
     */
    public final void setCurretPage(final int curretPage) {
        this.curretPage = curretPage;
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
