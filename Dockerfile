FROM openjdk:21-jdk-slim AS build
WORKDIR /app
COPY . /app
RUN ./gradlew clean build publishMavenJavaPublicationToLocalM2Repository -Xtest

FROM scratch AS platform-library-m2-cache
COPY --from=build /root/.m2 /root/.m2

