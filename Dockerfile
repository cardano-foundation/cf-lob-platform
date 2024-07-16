FROM openjdk:21-jdk-slim AS build
WORKDIR /app
COPY . /app

RUN ./gradlew clean -x test build
FROM openjdk:21-jdk-slim AS backend
WORKDIR /app
COPY --from=build /app/build/libs/*SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
