dependencies {
    implementation(project(":blockchain_common"))
    implementation(project(":support"))

    implementation("org.springframework.modulith:spring-modulith-api")
    implementation("org.springframework.modulith:spring-modulith-events-api")

    implementation("com.bloxbean.cardano:cardano-client-crypto")
    implementation("com.bloxbean.cardano:cardano-client-backend-blockfrost")
    //implementation("com.bloxbean.cardano:cardano-client-quicktx")
}
