package org.opengeoportal.dataingest.api.download;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.stereotype.Component;

/**
 * The Class RemoteDownloadServiceListner.
 */
@Component
public class RemoteDownloadServiceListner {

  /** The remote download service. */
  @Autowired
  private RemoteDownloadService remoteDownloadService;

  // /**
  // * Instantiates a new remote download service listner.
  // */
  // public RemoteDownloadServiceListner() {
  // this.remoteDownloadService = new RemoteDownloadService();
  // }

  /**
   * Prepare file.
   *
   * @param downloadRequest
   *          the download request
   */
  @JmsListener(destination = "fileRequestsQueue", containerFactory = "downloadRequestFactory")
  public final void prepareFile(final DownloadRequest downloadRequest) {
    try {
      remoteDownloadService.prepareDownload(downloadRequest);
    } catch (final Exception e) {
      e.printStackTrace();
      // An appropriate workaround.
    }

  }

  /**
   * Jackson jms message converter.
   *
   * @return the message converter
   */
  @Bean // Serialize message content to json
  private MessageConverter jacksonJmsMessageConverter() {
    final MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setTargetType(MessageType.TEXT);
    converter.setTypeIdPropertyName("_type");
    return converter;
  }

  /**
   * Download request factory.
   *
   * @param connectionFactory
   *          the connection factory
   * @param configurer
   *          the configurer
   * @return the jms listener container factory
   */
  @Bean
  private JmsListenerContainerFactory<?> downloadRequestFactory(
      final ConnectionFactory connectionFactory,
      final DefaultJmsListenerContainerFactoryConfigurer configurer) {
    final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    // This provides all boot's default to this factory, including the message
    // converter
    configurer.configure(factory, connectionFactory);
    return factory;
  }

}
