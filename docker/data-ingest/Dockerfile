FROM frolvlad/alpine-oraclejdk8:slim

MAINTAINER doublebyte <joana.simoes@geocat.net>

COPY ./Data-Ingest-0.0.1-SNAPSHOT.jar /Data-Ingest-0.0.1-SNAPSHOT.jar
RUN sh -c 'touch /app.jar'

EXPOSE 8080

ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /Data-Ingest-0.0.1-SNAPSHOT.jar" ]
