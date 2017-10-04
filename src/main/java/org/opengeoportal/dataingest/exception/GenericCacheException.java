package org.opengeoportal.dataingest.exception;

/**
 * Created by joana on 01/03/17.
 */
public class GenericCacheException extends Exception {

    /**
     * Constructor, which supports error messages.
     *
     * @param message error message
     */
    public GenericCacheException(final String message) {
        super(message);
    }
}
