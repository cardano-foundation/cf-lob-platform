import info.solidsoft.gradle.pitest.PitestTask
import org.gradle.api.JavaVersion.VERSION_21

plugins {
    java
    id("io.spring.dependency-management") version "1.1.5"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("info.solidsoft.pitest") version "1.15.0"
    id("maven-publish")
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "info.solidsoft.pitest")
    apply(plugin = "maven-publish")

    sourceSets {
        named("main") {
            java {
                setSrcDirs(listOf("src/main/java"))
            }
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            name = "sonatypeSnapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
        maven {
            name = "local"
            url = uri("file://${project.layout.buildDirectory}/repo")
        }
    }

    java {
        sourceCompatibility = VERSION_21
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    extra["springBootVersion"] = "3.3.3"
    extra["springCloudVersion"] = "2023.0.0"
    extra["springModulithVersion"] = "1.2.3"
    extra["jMoleculesVersion"] = "2023.1.0"

    dependencies {
        implementation("org.json:json:20211205") // TODO check if this is needed at all

        implementation("org.springframework.data:spring-data-envers")

        implementation("org.flywaydb:flyway-core")
        implementation("org.flywaydb:flyway-database-postgresql")

        implementation("com.bloxbean.cardano:cardano-client-crypto:0.5.1")

        // needed to store json via JPA in PostgreSQL
        implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.7.6")

        runtimeOnly("io.micrometer:micrometer-registry-prometheus")
        runtimeOnly("org.postgresql:postgresql")

        runtimeOnly("org.springframework.modulith:spring-modulith-actuator")

        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.boot:spring-boot-testcontainers")

        testImplementation("org.springframework.modulith:spring-modulith-starter-test")

        testImplementation("org.testcontainers:junit-jupiter")
        testImplementation("org.testcontainers:postgresql")

        runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")
        implementation("org.zalando:problem-spring-web-starter:0.29.1")
        implementation("io.vavr:vavr:0.10.4")
        implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

        implementation("org.scribe:scribe:1.3.7") // needed for OAuth 1.0 for NetSuite Module

        implementation("javax.xml.bind", "jaxb-api", "2.3.0")
        implementation("org.glassfish.jaxb:jaxb-runtime:2.3.2") // needed for OAuth 1.0 for NetSuite Module

        implementation("com.networknt:json-schema-validator:1.5.1")

        implementation("com.google.guava:guava:33.3.0-jre")

        implementation("org.apache.commons:commons-collections4:4.4")

        compileOnly("org.projectlombok:lombok:1.18.32")
        annotationProcessor("org.projectlombok:lombok:1.18.32")
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
        testCompileOnly("org.projectlombok:lombok:1.18.32")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
        testImplementation("io.rest-assured:rest-assured:5.5.0")
        testImplementation("org.wiremock:wiremock-standalone:3.6.0")
        testImplementation("net.jqwik:jqwik:1.9.0") // Jqwik for property-based testing
        testImplementation("org.assertj:assertj-core:3.26.0")
        testImplementation("org.pitest:pitest-junit5-plugin:1.2.1")
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
            mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
            mavenBom("org.jmolecules:jmolecules-bom:${property("jMoleculesVersion")}")
        }
    }

    tasks {
        val ENABLE_PREVIEW = "--enable-preview"

        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.compilerArgs.add(ENABLE_PREVIEW)
            //options.compilerArgs.add("-Xlint:preview")
        }

        withType<Jar> {
            archiveBaseName.set("lob-platform-${project.name}")
        }

        withType<Test> {
            useJUnitPlatform()
            jvmArgs(ENABLE_PREVIEW)
        }

        withType<PitestTask> {
            jvmArgs(ENABLE_PREVIEW)
        }

        withType<JavaExec> {
            jvmArgs(ENABLE_PREVIEW)
        }

    }

    pitest {
        //adds dependency to org.pitest:pitest-junit5-plugin and sets "testPlugin" to "junit5"
        jvmArgs.add("--enable-preview")
        targetClasses.set(setOf("org.cardanofoundation.lob.app.*"))
        targetTests.set(setOf("org.cardanofoundation.lob.app.*"))
        exportLineCoverage = true
        timestampedReports = false
        threads = 2
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifactId = "cf-lob-platform-" + project.name
            }
        }

        repositories {
            maven {
                name = "localM2"
                url = uri("${System.getProperty("user.home")}/.m2/repository")
            }
        }

    }

}
