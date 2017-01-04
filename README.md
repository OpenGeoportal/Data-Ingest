# Data-Ingest

GENERAL INFORMATION
===================
The Data Ingest component interfaces with Geoserver, allowing to list, upload, download, update and delete vector data stores and associated metadata. It provides both a REST API and a UI. The implementation uses the [Geoserver Manager](https://github.com/geosolutions-it/geoserver-manager) library to comunicate with Geoserver.

The [Data-Ingest](./Data-Ingest) folder contains the Mvn project which implements this application, while the [docker](./docker) folder contains an orchestration with the complete service stack for a development environment.

For detailed instructions on how-to run the docker composition which instantiates a service stack, please refer to the related [README](./docker/README.md) file.
