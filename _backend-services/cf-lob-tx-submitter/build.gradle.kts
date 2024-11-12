plugins {
	java
	id("io.spring.dependency-management") version "1.1.3"
    id("org.graalvm.buildtools.native") version "0.9.27"
    id("com.github.ben-manes.versions") version "0.48.0"
	id("org.springframework.boot") version "3.1.2"
}

group = "org.cardano.foundation"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
}

extra["springShellVersion"] = "3.1.2"

dependencies {
	implementation("org.springframework.shell:spring-shell-starter")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	compileOnly("org.projectlombok:lombok:1.18.34")
	annotationProcessor("org.projectlombok:lombok:1.18.34")

	testCompileOnly("org.projectlombok:lombok:1.18.34")
	testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

	implementation("com.bloxbean.cardano:cardano-client-crypto:0.6.2") // 0.6.2
	implementation("com.bloxbean.cardano:cardano-client-address:0.6.2")
	implementation("com.bloxbean.cardano:cardano-client-metadata:0.6.2")
	implementation("com.bloxbean.cardano:cardano-client-quicktx:0.6.2")
	implementation("com.bloxbean.cardano:cardano-client-backend-blockfrost:0.6.2")

	implementation("io.blockfrost:blockfrost-java:0.1.3")

	implementation("me.paulschwarz:spring-dotenv:4.0.0")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.shell:spring-shell-dependencies:${property("springShellVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
