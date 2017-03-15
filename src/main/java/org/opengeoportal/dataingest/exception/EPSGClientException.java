/*
 * @author Antonio
 */
package org.opengeoportal.dataingest.exception;

/**
 * Created by joana on 14/03/17.
 */
public class EPSGClientException extends Exception {
    /**
     * Constructor, which supports error messages.
     *
     * @param message
     *            error message
     */
    public EPSGClientException(final String message) {
        super(message);
    }

}
