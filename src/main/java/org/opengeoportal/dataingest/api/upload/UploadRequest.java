package org.opengeoportal.dataingest.api.upload;

import java.io.File;

/**
 * The Class UploadRequest.
 */
public class UploadRequest {

    /**
     * The workspace.
     */
    private String workspace;

    /**
     * The dataset.
     */
    private String dataset;

    /**
     * The store.
     */
    private String store;

    /**
     * True if it's a PUT request.
     */
    private boolean update;

    /**
     * Gets the str epsg.
     *
     * @return the str epsg
     */
    public String getStrEpsg() {
        return strEpsg;
    }

    /**
     * Sets the str epsg.
     *
     * @param strEpsg the new str epsg
     */
    public void setStrEpsg(final String strEpsg) {
        this.strEpsg = strEpsg;
    }

    /**
     * The zip file.
     */
    private File zipFile;

    /** The request token. */
    private long ticket;

    /** The str epsg. */
    private String strEpsg;

    /**
     * Instantiates a new upload request.
     *
     * @param workspace the workspace
     * @param store the datastore
     * @param dataset the dataset
     * @param zipFile the zip file
     * @param strEpsg the str epsg
     * @param ticket the ticket
     * @param update the update
     */
    public UploadRequest(final String workspace, final String store, final String dataset,
            final File zipFile, final String strEpsg, final long ticket, final boolean update) {

        this.workspace = workspace;
        this.store = store;
        this.dataset = dataset;
        this.zipFile = zipFile;
        this.ticket = ticket;
        this.strEpsg = strEpsg;
        this.update = update;
    }

    /**
     * empty class constructor.
     */
    public UploadRequest() {

    }

    /**
     * Gets the datastore.
     *
     * @return the datastore name
     */
    public final String getStore() {
        return store;
    }

    /**
     * Sets the datastore.
     *
     * @param store
     *            the new datastore
     */
    public final void setStore(final String store) {
        this.store = store;
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
     * @param ticket
     *            the new ticket
     */
    public void setTicket(final long ticket) {
        this.ticket = ticket;
    }

    /**
     * Checks if is update.
     *
     * @return true, if is update
     */
    public boolean isUpdate() {
        return update;
    }

    /**
     * Sets the update.
     *
     * @param update the new update
     */
    public void setUpdate(boolean update) {
        this.update = update;
    }
}
