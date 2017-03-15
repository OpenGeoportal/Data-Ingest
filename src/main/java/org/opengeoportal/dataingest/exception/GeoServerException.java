package org.opengeoportal.dataingest.exception;

/**
 * The Class GeoServerException.
 */
public class GeoServerException extends Exception {

    /** The msg. */
    private final String msg;

    /**
     * Instantiates a new geo server exception.
     *
     * @param msg the msg
     */
    public GeoServerException(final String msg) {
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
