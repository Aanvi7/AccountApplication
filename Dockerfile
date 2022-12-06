# Start with a base image contaning Java runtime
From openjdk:11-slim as build

#Information around who maintains the image
MAINTAINER shubh.com

# Add the application jar to the container 

COPY target/AccountApplication-0.0.1-SNAPSHOT.jar AccountApplication-0.0.1-SNAPSHOT.jar

#execute the application
ENTRYPOINT ["java","-jar","AccountApplication-0.0.1-SNAPSHOT.jar"]


