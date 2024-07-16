dependencies {

    implementation("org.springframework.modulith:spring-modulith-api")
    implementation("org.springframework.modulith:spring-modulith-events-api")

    implementation(project(":accounting_reporting_core"))
    implementation(project(":organisation"))
    implementation(project(":support"))
}
