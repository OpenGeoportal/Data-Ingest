package org.opengeoportal.dataingest.exception;

/**
 * Created by joana on 01/03/17.
 */
public class CacheCapacityException extends Exception {

    /**
     * Constructor, which supports error messages.
     *
     * @param message error message
     */
    public CacheCapacityException(final String message) {
        super(message);
    }
}
