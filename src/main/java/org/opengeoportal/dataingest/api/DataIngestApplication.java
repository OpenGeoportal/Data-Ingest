package org.opengeoportal.dataingest.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.EnableJms;

/**
 * Creates a RESTfull servrlet application. Introduces a default context.
 *
 * @author Joana Simoes
 * @version 1.0
 * @since 2017-01-11
 */
@SpringBootApplication
@SuppressWarnings("checkstyle:hideutilityclassconstructor")
@EnableJms
@EnableCaching
public class DataIngestApplication {
    /**
     * This is the main method which runs the web application.
     *
     * @param args Unused.
     * @throws Exception General exception
     */
    public static void main(final String[] args) throws Exception {
        final ConfigurableApplicationContext context = SpringApplication.run(DataIngestApplication.class, args);

    }

}
