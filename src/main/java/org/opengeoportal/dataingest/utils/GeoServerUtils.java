/*
 * @author Antonio
 */
package org.opengeoportal.dataingest.utils;

/**
 * Created by joana on 22/02/17.
 */
public final class GeoServerUtils {

    /**
     * Instantiates a new geo server utils.
     */
    private GeoServerUtils() {
    }

    /**
     * Gets the type name.
     *
     * @param workspace
     *            the workspace
     * @param dataset
     *            the dataset
     * @return the type name
     */
    public static String getTypeName(final String workspace,
            final String dataset) {
        return workspace + ":" + dataset;
    }

    /**
     * Gets the workspace.
     *
     * @param typerName
     *            the typer name
     * @return the workspace
     */
    public static String getWorkspace(final String typerName) {
        if (typerName == null) {
            return null;
        }
        return typerName.split(":")[0];
    }

    /**
     * Gets the dataset.
     *
     * @param typerName
     *            the typer name
     * @return the dataset
     */
    public static String getDataset(final String typerName) {
        if (typerName == null || !typerName.contains(":")) {
            return null;
        }
        return typerName.split(":")[1];
    }
}
