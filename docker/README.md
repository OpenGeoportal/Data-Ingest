GENERAL INFORMATION
===================
This composition instantiates support services for the Data-Ingest component. The shipped services are:

* Geoserver (2.10)
* ogpHarvester running on Tomcat 8 (Java 8)
* PostgreSQL (9.6) with PostGIS
* SOLR (6.3)

Each service runs in its own container. Decoupling applications into multiple containers makes it much easier to scale horizontally and reuse containers. As a general rule of thumb, we use the latest stable versions available for each software.

INSTALL
=======
REQUIREMENTS & INSTALLATION
---------------------------
For supporting version `2` of the compose syntax, you will need docker-compose >= 1.6 and docker >= 1.10.0
Instructions for installing the docker engine on different OS and flavours can be found here:

https://docs.docker.com/engine/installation/

Instructions for installing docker-compose can be found here:

https://docs.docker.com/compose/install/

Note that in some platforms, you will need to install docker-machine in addition to docker and compose.

After that, to build and run the system for the first time you want to go to the root directory and run _docker-compose_. If you run the system on your local machine, you can type:
 ```bash
  docker-compose up
```
Running the above, with the _-d_ flag, will cause it to run the container as daemons. In that case the output will not be redirected to stdout, but you may acccess it with:
 ```bash
 docker logs [container-name]
```
 To follow the logs in real time, use the _--follow_ flag:
 ```bash
 docker logs --follow [container-name]
 ```
This will download and compile the necessary images, and start the services.
Each service is wrapped into its own container. They are binded to the same host, and they are assigned fixed ports on startup.

You can stop the system, just by typing:
 ```bash
docker-compose stop
```
After creating the containers, you can start the system at any time with:
 ```bash
docker-compose start
```
Default Configuration
---------------------
The Tomcat container mounts a volume on the host, on `./webapps`. This is mounted to the webapps directory on Tomcat: `/usr/local/tomcat/webapps`, where servlet applications are deployed. By default, the ogpharvester is placed on this folder. If you want to deploy another version of the ogpharvester, or any other web application, just **place it on this folder prior to running the composition**.

Please be aware that the containers themselves are isolated from the host. For that reason, docker links were created to ensure that both GeoServer and the ogpHarvester are able to connect to the database service; The link was labeled `postgis` , and the url on jdbc.properties, from the ogpHarvester was updated to reflect that:
```JSON
 jdbc.url=jdbc:postgresql://postgis:5432/ogpharvester
```
**If you generate another war for the ogpharvester, be sure to update that link, or you will lose connectivity to the database.**
The database default credentials are:
* user: ogpharvester
* password: ogpharvester

After it is created, the solr server is initialized with one core, called `postgis`. You can add more cores, through the web UI.

Using the Services
------------------
When the system is up and running, you can access the different services, using the following urls:

Geoserver:
 ```bash
 https://[replace_me]:8081/geoserver
```
OgpHarvester:
 ```bash
 https://[replace_me]:8082/ogp-harvester/
```
Solr:
 ```
 https://[replace_me]:8983/solr/
```

If docker runs natively on your platform (_e.g._: Linux, some versions of Mac and some versions of Windows), just replace [replace_me] by _localhost_ (_e.g._: 127.0.0.1). Otherwise, replace it by the address of your docker machine. You can find this address, by typing:
```bash
docker-machine ip
```
The postgres database service exposes the default port, `5432`. You may connect to it, using the default credentials:
```bash
psql -h localhost -p 5432 -U ogpharvester
```

GeoServer
---------
Geoserver mounts its `/tmp` directory, on the host directory `./data`. This is very convenient, in order to load data into GeoServer. For instance, if you wanted to load the shapefile `boston_contours_wgs84.shp`, contained on folder `boston_contours`, you would first copy this folder to `docker/data`. Then you could create the import with:
```bash
curl -u admin:geoserver -XPOST -H "Content-type: application/json" -d @import.json "http://localhost:8081/geoserver/rest/imports"
```
If you wanted to load the data into workspace `topp`, your `import.json` would look like this:
```json
{
   "import": {
      "targetWorkspace": {
         "workspace": {
            "name": "topp"
         }
      },
      "data": {
        "type": "file",
        "file": "/tmp/boston_contours/boston_contours_wgs84.shp"
      }
   }
}
```
Then you could trigger the import with:
```bash
curl -u admin:geoserver -XPOST "http://localhost:8081/geoserver/rest/imports/0"
```
And check the results with:
```bash
curl -u admin:geoserver -XGET "http://localhost:8081/geoserver/rest/imports/1"
```
Please note that in this case, the import id is `1`, but you need to check which id returned by your first request and use that number.
Recreating Containers
=====================
For recreating a multi-container system, launched with docker compose, you must stop it first. Then you can remove the containers with:
```bash
 docker-compose rm
```
And then build them again with:
```bash
 docker-compose up
```
