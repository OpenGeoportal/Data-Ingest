package org.opengeoportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.EnableJms;

/**
 * Creates a RESTfull servrlet application.
 * Introduces a default context.
 *
 * @author Joana Simoes
 * @version 1.0
 * @since 2017-01-11
 */
@SpringBootApplication
@SuppressWarnings("checkstyle:hideutilityclassconstructor")
@EnableJms
public class DataIngestApplication {
    /**
     * This is the main method which runs the web application.
     *
     * @param args Unused.
     * @throws Exception General exception
     */
    public static void main(final String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(DataIngestApplication.class, args);
        
    }
    
   
}
