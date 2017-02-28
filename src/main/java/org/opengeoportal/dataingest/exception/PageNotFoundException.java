/*
 * @author Antonio
 */
package org.opengeoportal.dataingest.exception;

/**
 * The Class PageNotFoundException.
 */
public class PageNotFoundException extends Exception {

    /** The page number. */
    private final int pageNumber;

    /**
     * Instantiates a new page not found exception.
     *
     * @param pageNumber
     *            the page number
     */
    public PageNotFoundException(final int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * Gets the page number.
     *
     * @return the page number
     */
    public int getPageNumber() {
        return pageNumber;
    }
}
