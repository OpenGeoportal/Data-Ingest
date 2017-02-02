package org.opengeoportal.dataingest.download;

import javax.jms.ConnectionFactory;

import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.stereotype.Component;

@Component
public class RemoteDownloadServiceListner {

    private final RemoteDownloadService remoteDownloadService;

    
    public RemoteDownloadServiceListner() {
        this.remoteDownloadService = new RemoteDownloadService();
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

