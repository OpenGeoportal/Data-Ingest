package org.opengeoportal.dataingest.download;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class RemoteDownloadServiceListner {

    @Autowired
    private final RemoteDownloadService remoteDownloadService;

    
    public RemoteDownloadServiceListner(RemoteDownloadService remoteDownloadService) {
        this.remoteDownloadService = remoteDownloadService;
    }

    @JmsListener(destination = "fileRequestsQueue", containerFactory="downloadRequestFactory")
    public void prepareFile(DownloadRequest downloadRequest) { 
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        remoteDownloadService.prepareDownload(downloadRequest);

    }

}

