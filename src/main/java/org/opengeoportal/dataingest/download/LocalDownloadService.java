    package org.opengeoportal.dataingest.download;

import java.io.File;
import java.io.FileNotFoundException;

import org.opengeoportal.dataingest.exception.FileNotReadyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class LocalDownloadService {
    
        
        @Autowired
        private ApplicationContext context;
        

        private boolean isFilePresent(String workspace, String dataset) {
            
            return false;
        }
        
        private boolean isAValidRequest(String workspace, String dataset) {
            
            return true;
        }
        
        private void getFileFromRemote(String workspace, String dataset) {
            JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
            jmsTemplate.convertAndSend("fileRequestsQueue", new DownloadRequest(workspace,dataset));
        }
        
        /**
         * This is the method that we use for getting the file TODO: I think that we must cache this method        
            * @param workspace
            * @param dataset
            * @return The requested file
            * @throws FileNotFoundException File not found on OGS
            * @throws FileNotReadyException File not ready locally
         */
        public File getFile(String workspace, String dataset) throws FileNotFoundException, FileNotReadyException {
            
            if(!this.isAValidRequest(workspace, dataset)) {
                throw new FileNotFoundException();
            }
            
            if(this.isFilePresent(workspace, dataset)) {
                // TODO: return the file
                return null;
            } else {     
                getFileFromRemote(workspace, dataset);
                throw new FileNotReadyException();
            }
        }
        
        /**
         * This is the method force the download of the file, ignoring the cache   
            * @param workspace
            * @param dataset
            * @throws FileNotFoundException File not found on OGS
         */
        public void forceGetFile(String workspace, String dataset) throws FileNotFoundException {
           this.getFileFromRemote(workspace, dataset);
        }
        
        

}

  