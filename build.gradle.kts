import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jmailen.kotlinter") version "3.7.0"
    id("maven-publish")
    id("java-library")
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "com.valensas.common"
version = "1.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    if (project.hasProperty("AWS_REPO_URL")) {
        maven {
            this.url = uri(project.property("AWS_REPO_URL").toString())
            credentials(AwsCredentials::class) {
                this.accessKey = project.property("AWS_REPO_ADMIN_ACCESS_KEY").toString()
                this.secretKey = project.property("AWS_REPO_ADMIN_SECRET_KEY").toString()
            }
        }
    }
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            this.url = uri(project.property("AWS_REPO_URL").toString())
            credentials(AwsCredentials::class) {
                this.accessKey = project.property("AWS_REPO_USER_ACCESS_KEY").toString()
                this.secretKey = project.property("AWS_REPO_USER_SECRET_KEY").toString()
            }
        }
    }

    publications {
        create<MavenPublication>("artifact") {
            from(components["java"])
        }
    }
}