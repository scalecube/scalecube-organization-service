FROM openjdk:8
MAINTAINER support@jivygroup.com

ARG SERVICE_NAME
ARG EXECUTABLE_JAR
ENV SERVICE_NAME ${SERVICE_NAME}

COPY target/lib /opt/om2/lib
COPY target/${EXECUTABLE_JAR}.jar /opt/om2/app.jar
ENTRYPOINT ["/usr/bin/java", "-jar", "-Dlog4j.configurationFile=exchange-log4j2-cloud.xml", "/opt/om2/app.jar"]