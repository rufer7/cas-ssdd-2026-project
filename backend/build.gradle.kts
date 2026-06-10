import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
	java
	jacoco
	id("org.springframework.boot") version "4.0.7"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "7.3.0.8198"
}

group = "ch.ssdd"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencyLocking {
	lockAllConfigurations()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-h2console")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")

	implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sonar {
  properties {
    property("sonar.projectKey", "rufer7_cas-ssdd-2026-project")
    property("sonar.organization", "rufer7")
  }
}

tasks.named<JacocoReport>("jacocoTestReport") {
	dependsOn(tasks.named("test"))
	reports {
		xml.required.set(true)
	}
}

tasks.named("sonar") {
	dependsOn(tasks.named("jacocoTestReport"))
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.named("jacocoTestReport"))
}
