# fetch basic image
FROM maven:3.6-jdk-8

# application placed into /opt/app
RUN mkdir -p /opt/app
WORKDIR /opt/app

# utility for waiting other services
COPY wait-for /opt/app/

# selectively add the POM file and
# install dependencies
COPY pom.xml /opt/app/
RUN mvn install

# rest of the project
COPY src /opt/app/src
RUN mvn package

# local application port
EXPOSE 8080

ENV LOG_LEVEL INFO

# execute it
CMD ["mvn", "exec:java"]