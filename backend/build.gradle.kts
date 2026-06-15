import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
	java
	jacoco
	checkstyle
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "7.3.0.8198"
	id("com.diffplug.spotless") version "7.2.1"
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

// Un-lock configurations that IntelliJ or Gradle tooling resolve on their
// own and that you can't (or don't want to) pin:
configurations.configureEach {
	if (name.endsWith("Sources")           // *Sources variants IntelliJ downloads
		|| name.endsWith("Javadoc")        // *Javadoc variants
		|| name.endsWith("javadoc")
		|| name.endsWith("sources")
		|| name == "incrementalScalaAnalysisElements"
		|| !isCanBeResolved                // non-resolvable configs have no lock
	) {
		resolutionStrategy.deactivateDependencyLocking()
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-h2console")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	implementation("org.springframework.security:spring-security-oauth2-resource-server")
	implementation("org.springframework.security:spring-security-oauth2-jose")

	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")

	implementation(libs.owasp.java.html.sanitizer)
	implementation(libs.commons.io)

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

// --- Static analysis: Checkstyle ---
checkstyle {
	toolVersion = "10.21.2"
	configFile = file("config/checkstyle/checkstyle.xml")
	isIgnoreFailures = false
	maxWarnings = 0
}

// --- Code formatting: Spotless ---
spotless {
	java {
		target("src/**/*.java")
		importOrder()
		removeUnusedImports()
		trimTrailingWhitespace()
		endWithNewline()
	}
}

// --- Coverage: JaCoCo with an 80% minimum gate ---
tasks.named<JacocoReport>("jacocoTestReport") {
	dependsOn(tasks.named("test"))
	reports {
		xml.required.set(true)
	}
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
	dependsOn(tasks.named("test"))
	violationRules {
		rule {
			limit {
				counter = "INSTRUCTION"
				value = "COVEREDRATIO"
				minimum = "0.80".toBigDecimal()
			}
		}
	}
}

tasks.named("check") {
	dependsOn(tasks.named("jacocoTestCoverageVerification"))
}

tasks.named("sonar") {
	dependsOn(tasks.named("jacocoTestReport"))
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.named("jacocoTestReport"))
}
