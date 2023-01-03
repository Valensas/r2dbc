import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.1"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.jmailen.kotlinter") version "3.13.0"
    id("maven-publish")
    id("java-library")
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
}

group = "com.valensas.data"
version = "1.0.0"
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
    api("org.springframework.boot:spring-boot-starter-data-r2dbc")
    api("org.postgresql:postgresql")
    api("org.postgresql:r2dbc-postgresql")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    compileOnly("org.springframework.security:spring-security-oauth2-core")
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