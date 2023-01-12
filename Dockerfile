FROM gradle:jdk19
EXPOSE 8080
WORKDIR /app
COPY . .
RUN gradle build

ENTRYPOINT ["java","-jar","./build/libs/api-0.0.1-SNAPSHOT.jar"]