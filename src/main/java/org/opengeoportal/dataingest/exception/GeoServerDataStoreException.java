package org.opengeoportal.dataingest.exception;

/**
 * The Class GeoServerException.
 */
public class GeoServerDataStoreException extends Exception {

    /** The msg. */
    private final String msg;

    /**
     * Instantiates a new geo server exception.
     *
     * @param msg the msg
     */
    public GeoServerDataStoreException(final String msg) {
        this.msg = msg;
    }

    /**
     * Gets the msg.
     *
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }
}
