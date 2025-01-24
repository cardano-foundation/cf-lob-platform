plugins {
    id("org.sonarqube")
}

// Skipping the project from SonarQube analysis, since it doesn't have tests
sonarqube {
    isSkipProject = false
}

dependencies {

    implementation("org.springframework.modulith:spring-modulith-api")

    implementation(project(":support"))
}
