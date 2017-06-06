package org.opengeoportal.dataingest.exception;

/**
 * Created by joana on 15/05/17.
 */
public class NoMetadataException extends Exception {

    /**
     * Constructor, which supports error messages.
     *
     * @param message error message
     */
    public NoMetadataException(final String message) {
        super(message);
    }
}
