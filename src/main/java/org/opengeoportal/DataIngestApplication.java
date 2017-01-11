package org.opengeoportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Creates a RESTfull servrlet application.
 * Introduces a default context.
 *
 * @author Joana Simoes
 * @version 1.0
 * @since   2017-01-11
 */
@RestController
@EnableAutoConfiguration
public class DataIngestApplication {

    /**
     * Annotation for mapping a default web request onto specific
     * handler method.
     * @return Nothing
     */

    @RequestMapping("/")

    /**
     * This is a demonstration method which returns a string.
     * @return String Demo string, to be displayed by the web application.
     */

    final String home() {
        return "Data Ingest";
    }

    /**
     * This is the main method which runs the web application.
     * @param args Unused.
     * @exception Exception General exception
     */

    public static void main(final String[] args) throws Exception {
        SpringApplication.run(DataIngestApplication.class, args);
    }
}
