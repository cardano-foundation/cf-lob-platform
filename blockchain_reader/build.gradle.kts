dependencies {
    implementation(project(":blockchain_common"))
    implementation(project(":support"))

    implementation("org.springframework.modulith:spring-modulith-api")
    implementation("org.springframework.modulith:spring-modulith-events-api")

    implementation("com.bloxbean.cardano:cardano-client-crypto")
    implementation("com.bloxbean.cardano:cardano-client-backend-blockfrost")
    implementation("com.bloxbean.cardano:cardano-client-quicktx")

    implementation("com.bloxbean.cardano:yaci-store-spring-boot-starter:0.1.0-rc5")
    implementation("com.bloxbean.cardano:yaci-store-blocks-spring-boot-starter:0.1.0-rc5")
    implementation("com.bloxbean.cardano:yaci-store-transaction-spring-boot-starter:0.1.0-rc5")
    implementation("com.bloxbean.cardano:yaci-store-metadata-spring-boot-starter:0.1.0-rc5")
}
