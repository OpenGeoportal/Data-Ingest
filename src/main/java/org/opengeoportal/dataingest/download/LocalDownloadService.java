    package org.opengeoportal.dataingest.download;

import java.io.File;
import java.io.FileNotFoundException;

import javax.jms.ConnectionFactory;

import org.opengeoportal.DataIngestApplication;
import org.opengeoportal.dataingest.exception.FileNotReadyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = DataIngestApplication.class, loader = SpringApplicationContextLoader.class)
public class LocalDownloadService {
    
        @Autowired(required = false)
        private JmsTemplate jmsTemplate;
        
        private boolean isFilePresent(String workspace, String dataset) {
            
            return false;
        }
        
        private boolean isAValidRequest(String workspace, String dataset) {
            
            return true;
        }
        
        private void getFileFromRemote(String workspace, String dataset) {
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
        
        @Bean // Serialize message content to json
        private MessageConverter jacksonJmsMessageConverter() {
            MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
            converter.setTargetType(MessageType.TEXT);
            converter.setTypeIdPropertyName("_type");
            return converter;
        }
        
        @Bean
        private JmsListenerContainerFactory<?> downloadRequestFactory(ConnectionFactory connectionFactory,
                                                        DefaultJmsListenerContainerFactoryConfigurer configurer) {
            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            // This provides all boot's default to this factory, including the message converter
            configurer.configure(factory, connectionFactory);
            return factory;
        }

}

  