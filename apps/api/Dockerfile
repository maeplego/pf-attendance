FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY src src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget
COPY --from=build /src/target/attendance-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8019
ENTRYPOINT ["java", "-jar", "app.jar"]
