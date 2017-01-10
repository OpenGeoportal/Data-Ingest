package org.opengeoportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class DataIngestApplication {

    @RequestMapping("/")
    String home() {
        return "Data Ingest";
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(DataIngestApplication.class, args);
    }
}