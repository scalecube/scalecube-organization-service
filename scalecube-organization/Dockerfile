FROM openjdk:8
MAINTAINER support@jivygroup.com

ARG SERVICE_NAME
ARG EXECUTABLE_JAR
ENV SERVICE_NAME ${SERVICE_NAME}

WORKDIR /opt/om2

COPY target/lib lib
COPY target/${EXECUTABLE_JAR}.jar app.jar

ENTRYPOINT exec java $JAVA_OPTS -Dlog4j.configurationFile=log4j2.xml -jar app.jar
