/*
 * @author Antonio
 */
package org.opengeoportal.dataingest.api.upload;

import java.io.File;

/**
 * The Class UploadRequest.
 */
public class UploadRequest {

    /** The workspace. */
    private String workspace;

    /** The dataset. */
    private String dataset;

    /** The zip file. */
    private File zipFile;
    
    /** The crs of the shapefile. */
    private String crs;
    
    /**  The request token. */
    private long ticket;

    /**
     * Instantiates a new download request.
     *
     * @param workspace            the workspace
     * @param dataset            the dataset
     * @param zipFile            the zip file
     * @param crs the crs
     * @param ticket the ticket
     */
    public UploadRequest(final String workspace, final String dataset,
            final File zipFile, final String crs, long ticket) {

        this.workspace = workspace;
        this.dataset = dataset;
        this.zipFile = zipFile;
        this.crs = crs;
        this.ticket = ticket;
    }

    /**
     * empty class constructor.
     */
    public UploadRequest() {

    }

    /**
     * Gets the workspace.
     *
     * @return the workspace name
     */
    public final String getWorkspace() {
        return workspace;
    }

    /**
     * Sets the workspace.
     *
     * @param workspace
     *            the new workspace
     */
    public final void setWorkspace(final String workspace) {
        this.workspace = workspace;
    }

    /**
     * Gets the dataset.
     *
     * @return the dataset name
     */
    public final String getDataset() {
        return dataset;
    }

    /**
     * Sets the dataset.
     *
     * @param dataset
     *            the new dataset
     */
    public final void setDataset(final String dataset) {
        this.dataset = dataset;
    }

    /**
     * Gets the zip file.
     *
     * @return the zip file
     */
    public File getZipFile() {
        return zipFile;
    }

    /**
     * Sets the zip file.
     *
     * @param zipFile
     *            the new zip file
     */
    public void setZipFile(final File zipFile) {
        this.zipFile = zipFile;
    }

    /**
     * Gets the crs.
     *
     * @return the crs
     */
    public String getCrs() {
        return crs;
    }

    /**
     * Sets the crs.
     *
     * @param crs the new crs
     */
    public void setCrs(String crs) {
        this.crs = crs;
    }

    /**
     * Gets the ticket.
     *
     * @return the ticket
     */
    public long getTicket() {
        return ticket;
    }

    /**
     * Sets the ticket.
     *
     * @param ticket the new ticket
     */
    public void setTicket(long ticket) {
        this.ticket = ticket;
    }
}
