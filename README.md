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

There is a batch of unit tests associated to this build. If you want to build _without_ the tests, run:

```bash
mvn clean install  -DskipTests
```
On the other hand, if you just want to run the test suite, type:
```bash
mvn test
```
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

On a bash shell, you would set it permanently by adding this instruction to your `~/.bashrc`, `~/.bash_profile`.

In OsX the docker daemon __does not__ listen on this address, so you should __not__ set the `DOCKER_HOST` variable.

You can, however, _fake_ the unix docker daemon with this workaround:

```bash
socat TCP-LISTEN:2375,reuseaddr,fork,bind=localhost UNIX-CONNECT:/var/run/docker.sock &
```

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
File Cache
==========
For improving the speed of downloads, we implemented a file cache, which stores physical files on disk. The cache uses a memory structure (an hashmap), to store the references to the physical files.
## Cache Parameters
When the application starts, the file cache is initialized with three parameters, which are set on the application.properties file:
* `cache.capacity`: the limit of the cache on disk (in bytes)
* `cache path`: the disk path of the cache
* `cache.name`: the name of the cache, which will be the name of a subdirectory, to be created under under `cache.path`

## Cache Initialization & Termination
The `cache.path` parameter can be empty, in which case it will default to the `TMP` directory of the OS. All other variables are mandatory.

As an example, for _cache.path=/tmp_ and _cache.name=cache_, a directory named `/tmp/cache` would be created on startup. If that directory already existed, and had adequated permissions, it would be reused; otherwise an error would be thrown. Please not that any errors in the cache initialization would cause the application to not start.

When the application exits, the cache directory is emptied and removed.

## Cache Eviction
For evicting the cache, we have implemented a [LRU](https://en.wikipedia.org/wiki/Cache_replacement_policies#Least_Recently_Used_.28LRU.29) policy. This is a well-known algorithm, which discards the least recently used items first.

![LRU Cache](https://raw.githubusercontent.com/OpenGeoportal/Data-Ingest/master/lru.png "LRU Cache")

The eviction is triggered when the file we request for download is going to increase the cache till, or over its capacity. If we request to download a file which is larger than the cache capacity, an error will be thrown, prompting the user to review the cache configuration; this error means that there is no way this file can be downloaded, unless we start the application with a larger cache.

## Cache Invalidation
In principle, subsequent requests for the same `workspace:dataset` will land in the cache. However, there are some situations at which we want to invalidate the cache and force a new download request. As a rule of thumb, we do not want to use the cache, when it no longer represents an accurate image of reality. More specifically, if the cache re-creates the files available on Geoserver, we know that a file is no longer valid, if:
* We remove that file from Geoserver.
* We update that file in GeoServer.

For removal and update events whithin the Data-Ingest API, we can trigger a cache invalidation which will force a new download request. However, GeoServer can also be accessed outside the API, and in that case we have no way of accessing these events. To mitigate this problem, we implemented a couple of strategies. In order to identify updated files, when a file is requested from the cache, we compare the size of the file on disk with the size on GeoServer (by issuing a request to the size headers). This will not identify the scenarios when a file is updated and has the same size, but will identify the most common scenario when a file is updated its size changes.
As a last resource, to prevent the cache to live forever, we also implemented a cache validity (in seconds), which is configurable in.application properties with variable: `param.download.max.age.file`. Everytime we hit the cache, we check the age of that file, and decide if it should be invalidated.

Finally, to free disk resources from files that no longer exist in GeoServer, we added a function that removes a file from the cache, whenever GeoServer does not list this file anymore. This function is triggered in the download request, which has a call to check if the file exists in GeoServer.

## Update / Upload methods

The data ingest API let you to upload shapefiles directly to the connected GeoServer instance. 

You can upload a file to an existing workspace WWWWW to define the new datastore and published layer DDDDD by send a POST request to http://YOURHOST:PORT/workspaces/WWWWW/datasets/DDDDD

_*Example:* curl -v -F file=@/PATHTOYOURFILE/DDDDD.zip -X POST http://YOURHOST:PORT/workspaces/WWWWW/datasets/DDDDD_

You can update an existing datastore by uploading the shape file DDDDD.zip to an existing workspace WWWWW to the datastore DDDDD by send a PUT request to http://YOURHOST:PORT/workspaces/WWWWW/datasets/DDDDD

_*Example:* curl -v -F file=@/PATHTOYOURFILE/DDDDD.zip -X PUT http://YOURHOST:PORT/workspaces/WWWWW/datasets/DDDDD_

These methods are asynchronous with the real upload, so they just give a direct feedback on validation of the file and the ticket number associated with the request (_TICKETNUM_). To check the status of the request the method /checkUploadStatus/TICKETNUM is provided.

### The submitted file must be validate trough the following checks:

1. The uploaded file must be archived in ZIP format
2. The name of the zip file must be the same of the datastore and of the layer where it will be uploaded
3. The files in the zip must have the same name as the zip file
4. The zip file must contain a single shapefile
5. The shapefile must contain a crs value recognised




