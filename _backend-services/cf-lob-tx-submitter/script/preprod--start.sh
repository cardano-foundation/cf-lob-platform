export SPRING_CONFIG_LOCATION=classpath:/application.properties
export SPRING_PROFILES_ACTIVE=dev--preprod
./gradlew clean build && java -jar build/libs/voting-admin-app-1.0.0-SNAPSHOT.jar
