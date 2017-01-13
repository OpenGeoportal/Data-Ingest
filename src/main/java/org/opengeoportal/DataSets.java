package org.opengeoportal;

/**
 *
 * Dataset list model.
 *
 * @author Joana Simoes
 * @version 1.0
 * @since   2017-01-13
 */
public class DataSets {

    /**
     * Textual representation of the dataset list.
     */
    private final String content;

    /**
     * Constructor for the DataSets object.
     * @param content list of datasets.
     */
    public DataSets(final String content) {
        this.content = content;
    }

    /**
     * Getter for the dataset list.
     * @return list of datasets.
     */
    public final String getContent() {
        return content;
    }
}
