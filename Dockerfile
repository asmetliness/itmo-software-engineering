FROM gradle:7.4.1-jdk8
EXPOSE 8080
WORKDIR /app
COPY . .
RUN gradle build

ENTRYPOINT ["java","-jar","./build/libs/api-0.0.1-SNAPSHOT.jar"]