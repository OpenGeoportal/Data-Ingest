# Data-Ingest

GENERAL INFORMATION
===================
The Data Ingest component interfaces with Geoserver, allowing to list, upload, download, update and delete vector data stores and associated metadata. It provides both a REST API and a UI. The implementation uses the [Geoserver Manager](https://github.com/geosolutions-it/geoserver-manager) library to comunicate with Geoserver.

This is a [Spring Boot](https://projects.spring.io/spring-boot/) project. You can ompile and run it on the command line with:

```bash
mvn spring-bot:run
```

This will build a servlet and deploy it on a webserver started on port `8080`:

```bash
http://localhost:8080
```

The build uses the [checkstyle Maven plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/) for reporting on the style used by developers and the [findbugs Maven plugin](http://gleclaire.github.io/findbugs-maven-plugin/) which looks for bug patterns. If you want to deactivate them on a build, run:

```bash
mvn clean install  -Drelax
```

The [docker](./docker) folder contains an orchestration with the complete service stack for a development environment.

For detailed instructions on how-to run the docker composition which instantiates a service stack, please refer to the related [README](./docker/README.md) file.
