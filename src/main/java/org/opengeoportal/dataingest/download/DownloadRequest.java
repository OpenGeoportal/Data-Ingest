    package org.opengeoportal.dataingest.download;

import java.io.Serializable;

public class DownloadRequest implements Serializable{
        
        private String workspace;
        private String dataset;
        
        public DownloadRequest(String workspace, String dataset) {

            this.workspace = workspace;
            this.dataset = dataset;
        }
        
        public DownloadRequest() {

        }
        
        public String getWorkspace() {
            return workspace;
        }
        public void setWorkspace(String workspace) {
            this.workspace = workspace;
        }
        public String getDataset() {
            return dataset;
        }
        public void setDataset(String dataset) {
            this.dataset = dataset;
        }
        
        

}

  