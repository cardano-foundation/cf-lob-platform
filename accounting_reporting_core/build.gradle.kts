dependencies {
    implementation("org.jmolecules:jmolecules-events")
    implementation("org.jmolecules:jmolecules-ddd")

    implementation("org.springframework.modulith:spring-modulith-api")
    implementation("org.springframework.modulith:spring-modulith-events-api")

    implementation("org.springframework.boot:spring-boot-starter-security:3.2.4")

    implementation(project(":blockchain_reader"))
    implementation(project(":organisation"))
    implementation(project(":support"))
}
