# Data-Ingest

GENERAL INFORMATION
===================
The Data Ingest component interfaces with Geoserver, allowing to list, upload, download, update and delete vector data stores and associated metadata. It provides both a REST API and a UI. The implementation uses the [Geoserver Manager](https://github.com/geosolutions-it/geoserver-manager) library and the Geotools [WFS-NG](http://docs.geotools.org/latest/userguide/library/data/wfs-ng.html) plugin to comunicate with Geoserver.

This is a [Spring Boot](https://projects.spring.io/spring-boot/) project. You can compile and run it on the command line with:

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

Tests
-----
The unit tests use a [docker junit rule](https://github.com/klousiaj/docker-junit-rule), which launches a container with Geoserver. In some cases, in order to get it working, you may need to set the `DOCKER_HOST` environment variable. In nix systems:

```bash
 export DOCKER_HOST=$DOCKER_HOST:$DOCKER_PORT
```

For instance:

```bash
 export DOCKER_HOST=localhost:2375
```
In OSX, you may need to bind the docker host to the unix socket:

```bash
 export DOCKER_HOST=unix:///var/run/docker.sock
```

On a bash shell, you would set it permanently by adding this instruction to your `~/.bashrc`, `~/.bash_profile`.

Important Information:
----------------------
This repository uses large files; to read them correctly, you must enable the support to [Git Large File Storage](https://git-lfs.github.com), by following these steps:
* Download and install the [git-lfs extension](https://git-lfs.github.com).
* Initialize it, by typing `git lfs install`
* Change to the root of this folder (if you are not already there) and type `git lfs track "*.zip`

You only need to do this once. If you find out that the large zip file does not download correctly, you can fix it by removing it and checking it out again:

```bash
rm docker/geoserver/boston_contours.zip
```

```bash
git checkout docker/geoserver/boston_contours.zip
```
