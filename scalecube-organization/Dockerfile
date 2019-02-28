FROM openjdk:8

ARG LOG4J_CONFIG="-Dlog4j.configurationFile=log4j2.xml"
ARG EXECUTABLE_JAR

WORKDIR /opt/scalecube

COPY target/lib lib
COPY target/${EXECUTABLE_JAR}.jar app.jar

ENTRYPOINT exec java $JAVA_OPTS $LOG4J_CONFIG -jar app.jar
