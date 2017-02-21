package org.opengeoportal.dataingest.api.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.opengeoportal.dataingest.api.GeoserverDataStore;
import org.opengeoportal.dataingest.exception.FileNotReadyException;
import org.opengeoportal.dataingest.utils.FileManager;
import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * The Class LocalDownloadService.
 */
@Component
public class LocalDownloadService {

    /**
     * The GeoServer URL (from the application.properties).
     */
    @Value("${geoserver.url}")
    private String geoserverUrl;

    /**
     * The GeoServer Username (from the application.properties).
     */
    @Value("${geoserver.username}")
    private String geoserverUsername;

    /**
     * The GeoServer Password (from the application.properties).
     */
    @Value("${geoserver.password}")
    private String geoserverPassword;

    /** Max age allowed for files in cache. */
    @Value("${param.download.max.age.file}")
    private long maxDownloadFileAgeInSeconds;

    /** The context. */
    @Autowired
    private ApplicationContext context;

    /**
     * Check if data exists.
     *
     * @param workspace
     *            the workspace
     * @param dataset
     *            the dataset
     * @return true
     */
    private boolean isAValidRequest(final String workspace,
            final String dataset) {

        /*
         * Find a better way than asking directly for the file (some method in
         * org.geotools.data.store.ContentDataStore?)
         */

        try {

            final GeoserverDataStore gds = new GeoserverDataStore(geoserverUrl);
            return (gds.getLayerInfo(geoserverUrl, workspace, dataset)
                    .size() > 0);

        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the file from remote.
     *
     * @param workspace            the workspace
     * @param dataset            the dataset
     */
    private void getFileFromRemote(final String workspace,
            final String dataset) {
        final JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        jmsTemplate.convertAndSend("fileRequestsQueue",
                new DownloadRequest(workspace, dataset));
    }

    /**
     * This is the method that we use for getting the file. Consider to cache
     * this method.
     *
     * @param workspace            the workspace
     * @param dataset            the dataset
     * @return The requested file
     * @throws FileNotReadyException             File not ready locally
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public final File getFile(final String workspace, final String dataset)
            throws FileNotReadyException, IOException {

        // this data exists
        if (!this.isAValidRequest(workspace, dataset)) {
            throw new FileNotFoundException();
        }

        final String fileName = FileNameUtils.getFullPathZipFile(workspace,
                dataset);
        FileManager fileM = null;

        try {
            // The files already exists and is not locked (downloading)
            fileM = new FileManager(fileName);
            if (fileM.getFileAgeinSeconds() > maxDownloadFileAgeInSeconds) {
                getFileFromRemote(workspace, dataset);
                throw new FileNotReadyException();
            } else {
                return fileM.getFile();
            }
        } catch (FileNotReadyException fnrex) {
            // the files doesn't exists (to check: it can be a problem if maxDownloadFileAgeInSeconds is smaller then the download time)
            getFileFromRemote(workspace, dataset);
            throw fnrex;
        }

    }

    /**
     * This is the method force the download of the file, ignoring the cache .
     *
     * @param workspace
     *            the workspace
     * @param dataset
     *            the dataset
     * @throws FileNotFoundException
     *             File not found on OGS
     */
    public final void forceGetFile(final String workspace, final String dataset)
            throws FileNotFoundException {
        this.getFileFromRemote(workspace, dataset);
    }

}
