FROM openjdk:8
MAINTAINER support@jivygroup.com

ARG EXECUTABLE_JAR

WORKDIR /opt/scalecube

COPY target/lib lib
COPY target/${EXECUTABLE_JAR}.jar app.jar

ENTRYPOINT exec java $JAVA_OPTS -Dlog4j.configurationFile=log4j2.xml -jar app.jar
