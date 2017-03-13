package org.opengeoportal.dataingest.api.upload;

import java.io.File;
import java.io.IOException;

import org.opengeoportal.dataingest.exception.CacheCapacityException;
import org.opengeoportal.dataingest.exception.FileNotReadyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * The Class LocalUploadService.
 */
@Component
public class LocalUploadService {

    /**
     * The application context.
     */
    @Autowired
    private ApplicationContext context;

    /**
     * Upload file.
     *
     * @param workspace
     *            the workspace
     * @param dataset
     *            the dataset
     * @param zipFile
     *            the zip file
     * @param isUpdate
     *            the is update
     * @throws FileNotReadyException
     *             the file not ready exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws Exception
     *             the exception
     * @throws CacheCapacityException
     *             the cache capacity exception
     */
    public final void uploadFile(final String workspace, final String dataset,
            final File zipFile, final boolean isUpdate)
            throws FileNotReadyException, IOException, java.lang.Exception,
            CacheCapacityException {

        final JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        jmsTemplate.convertAndSend("uploadQueue",
                new UploadRequest(workspace, dataset, zipFile));

    }

}
