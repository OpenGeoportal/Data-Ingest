package org.opengeoportal.dataingest.download;

public class RemoteDownloadService {
    
    public void prepareDownload(DownloadRequest downloadRequest) {
        
        System.out.println("Received <" + downloadRequest.getDataset() + ">");
        
    }



}

