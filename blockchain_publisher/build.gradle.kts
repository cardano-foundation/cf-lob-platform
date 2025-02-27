dependencies {
    implementation(project(":accounting_reporting_core"))
    implementation(project(":organisation"))
    implementation(project(":support"))
    implementation(project(":blockchain_common"))
    implementation(project(":blockchain_reader"))

    implementation("org.springframework.modulith:spring-modulith-api")
    implementation("org.springframework.modulith:spring-modulith-events-api")

    implementation("com.bloxbean.cardano:cardano-client-crypto")
    implementation("com.bloxbean.cardano:cardano-client-backend-blockfrost")
    implementation("com.bloxbean.cardano:cardano-client-quicktx")

    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-core")
    testImplementation("org.springframework.modulith:spring-modulith-events-jdbc")
    testImplementation("org.springframework.boot:spring-boot-starter-validation")

}
