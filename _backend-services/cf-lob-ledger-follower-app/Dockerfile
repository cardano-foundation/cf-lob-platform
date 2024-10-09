FROM openjdk:21-jdk-slim AS build
WORKDIR /app
COPY . /app
RUN ./gradlew clean build

