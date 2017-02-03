package org.opengeoportal.dataingest.api.download;

/**
 * The Class DownloadRequest.
 *
 * @author antos
 */
public class DownloadRequest {

    /**
     * the name of the workspace.
     */
    private String workspace;
    /**
     * the name of the dataset.
     */
    private String dataset;

    /**
     * Instantiates a new download request.
     *
     * @param workspace the workspace
     * @param dataset the dataset
     */
    public DownloadRequest(final String workspace, final String dataset) {

        this.workspace = workspace;
        this.dataset = dataset;
    }

    /**
     * empty class constructor.
     */
    public DownloadRequest() {

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
     * @param workspace the new workspace
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
     * @param dataset the new dataset
     */
    public final void setDataset(final String dataset) {
        this.dataset = dataset;
      }
    }
