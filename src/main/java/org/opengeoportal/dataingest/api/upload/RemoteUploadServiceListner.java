package org.opengeoportal.dataingest.api.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * The Class RemoteUploadServiceListner.
 */
@Component
public class RemoteUploadServiceListner {

    /** The remote uplad service. */
    @Autowired
    private RemoteUploadService remoteUpladService;

    /**
     * Prepare file.
     *
     * @param uploadRequest
     *            the upload request
     */
    @JmsListener(destination = "uploadQueue", containerFactory = "myContainerFactory")
    public final void prepareFile(final UploadRequest uploadRequest) {
        try {
            remoteUpladService.sendFile(uploadRequest);
        } catch (final Exception e) {
            e.printStackTrace();
            // An appropriate workaround.
        }

    }

}
