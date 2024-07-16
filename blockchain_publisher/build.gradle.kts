dependencies {
    implementation(project(":accounting_reporting_core"))
    implementation(project(":organisation"))
    implementation(project(":support"))

    implementation("org.springframework.modulith:spring-modulith-api")
    implementation("org.springframework.modulith:spring-modulith-events-api")

    implementation("com.bloxbean.cardano:cardano-client-crypto:0.5.1")
    implementation("com.bloxbean.cardano:cardano-client-backend-blockfrost:0.5.1")
    implementation("com.bloxbean.cardano:cardano-client-quicktx:0.5.1")

}
