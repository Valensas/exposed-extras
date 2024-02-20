import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jmailen.kotlinter") version "4.2.0"
    id("maven-publish")
    id("java-library")
    id("jacoco")
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.1.1"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
}

group = "com.valensas"
version = "0.1.0"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}


dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-web")

    api("org.jetbrains.exposed:exposed-spring-boot-starter:0.47.0")
    api("org.jetbrains.exposed:exposed-java-time:0.47.0")
    api("org.jetbrains.exposed:exposed-json:0.47.0")

    // For vendor specific implementations
    compileOnly("org.postgresql:postgresql")

    // Kotlin reflection support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.flywaydb:flyway-core")
    testImplementation("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

signing {
    val keyId = System.getenv("SIGNING_KEYID")
    val secretKey = System.getenv("SIGNING_SECRETKEY")
    val passphrase = System.getenv("SIGNING_PASSPHRASE")

    useInMemoryPgpKeys(keyId, secretKey, passphrase)
}

centralPortal {
    username = System.getenv("SONATYPE_USERNAME")
    password = System.getenv("SONATYPE_PASSWORD")

    pom {
        name = "Exposed Extras"
        description = "An utility library that adds features to Exposed ORM"
        url = "https://valensas.com/"
        scm {
            url = "https://github.com/Valensas/exposed-extras"
        }

        licenses {
            license {
                name.set("MIT License")
                url.set("https://mit-license.org")
            }
        }

        developers {
            developer {
                id.set("0")
                name.set("Valensas")
                email.set("info@valensas.com")
            }
        }
    }
}