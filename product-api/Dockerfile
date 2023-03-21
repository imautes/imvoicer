FROM maven:3.9.0-eclipse-temurin-19-alpine AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests=true

FROM openjdk:19-alpine
COPY --from=build /home/app/target/app.jar /usr/local/lib/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/usr/local/lib/app.jar"]
