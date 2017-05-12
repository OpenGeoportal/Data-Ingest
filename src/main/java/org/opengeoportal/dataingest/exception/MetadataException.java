package org.opengeoportal.dataingest.exception;

/**
 * Created by joana on 12/05/17.
 */

public class MetadataException extends Exception {

    /**
     * Constructor, which supports error messages.
     *
     * @param message error message
     */
    public MetadataException(final String message) {
        super(message);
    }
}

