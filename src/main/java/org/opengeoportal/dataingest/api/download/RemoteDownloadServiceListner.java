package org.opengeoportal.dataingest.api.download;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * The Class RemoteDownloadServiceListner.
 */
@Component
public class RemoteDownloadServiceListner {

    /** The remote download service. */
    @Autowired
    private RemoteDownloadService remoteDownloadService;

    /**
     * Prepare file.
     *
     * @param downloadRequest
     *            the download request
     */
    @JmsListener(destination = "fileRequestsQueue", containerFactory = "myContainerFactory")
    public final void prepareFile(final DownloadRequest downloadRequest) {
        try {
            remoteDownloadService.prepareDownload(downloadRequest);
        } catch (final Exception e) {
            e.printStackTrace();
            // An appropriate workaround.
        }

    }

}
