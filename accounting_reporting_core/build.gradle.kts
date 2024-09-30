dependencies {
    implementation("org.jmolecules:jmolecules-events")
    implementation("org.jmolecules:jmolecules-ddd")

    implementation("org.springframework.modulith:spring-modulith-api")
    implementation("org.springframework.modulith:spring-modulith-events-api")

    implementation(project(":blockchain_reader"))
    implementation(project(":organisation"))
    implementation(project(":support"))
}
