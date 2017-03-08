package org.opengeoportal.dataingest.exception;

/**
 * Created by joana on 08/03/17.
 */
public class WFSException extends Exception {

    /**
     * Constructor, which supports error messages.
     *
     * @param message error message
     */
    public WFSException(String message) {
        super(message);
    }
}
