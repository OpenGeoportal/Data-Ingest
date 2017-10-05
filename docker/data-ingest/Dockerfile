FROM frolvlad/alpine-oraclejdk8:slim

MAINTAINER doublebyte <joana.simoes@geocat.net>

RUN apk update && apk add bash

COPY ./Data-Ingest-0.0.1-SNAPSHOT.jar /Data-Ingest-0.0.1-SNAPSHOT.jar
RUN sh -c 'touch /Data-Ingest-0.0.1-SNAPSHOT.jar'

EXPOSE 8080

#ENV JAVA_OPTS=""
COPY ./wait-for-it.sh /wait-for-it.sh
#ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /Data-Ingest-0.0.1-SNAPSHOT.jar" ]
