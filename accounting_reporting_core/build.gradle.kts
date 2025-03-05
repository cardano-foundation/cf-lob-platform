dependencies {
    implementation("org.jmolecules:jmolecules-events")
    implementation("org.jmolecules:jmolecules-ddd")

    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation(project(":blockchain_reader"))
    implementation(project(":organisation"))
    implementation(project(":support"))

    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
}
