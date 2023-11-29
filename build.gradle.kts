import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jmailen.kotlinter") version "4.1.0"
    id("maven-publish")
    id("java-library")
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"

    id("org.graalvm.buildtools.native") version "0.9.28"
    id("com.github.ben-manes.versions") version "0.50.0"
}

group = "com.valensas.data"
version = "1.12.8"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    if (project.hasProperty("GITLAB_REPO_URL")) {
        maven {
            name = "Gitlab"
            url = uri(project.property("GITLAB_REPO_URL").toString())
            credentials(HttpHeaderCredentials::class.java) {
                name = project.findProperty("GITLAB_TOKEN_NAME")?.toString()
                value = project.findProperty("GITLAB_TOKEN")?.toString()
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }
        }
    }
    mavenLocal()
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-r2dbc")
    api("org.postgresql:r2dbc-postgresql")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")


    compileOnly("org.springframework.security:spring-security-oauth2-core")

    testImplementation("org.flywaydb:flyway-core")
    testRuntimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-oauth2-core")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    repositories {
        if (System.getenv("CI_API_V4_URL") != null) {
            maven {
                name = "Gitlab"
                url = uri("${System.getenv("CI_API_V4_URL")}/projects/${System.getenv("CI_PROJECT_ID")}/packages/maven")
                credentials(HttpHeaderCredentials::class.java) {
                    name = "Job-Token"
                    value = System.getenv("CI_JOB_TOKEN")
                }
                authentication {
                    create("header", HttpHeaderAuthentication::class)
                }
            }
        }
    }

    publications {
        create<MavenPublication>("artifact") {
            from(components["java"])
        }
    }
}

tasks.formatKotlin {
    setDependsOn(dependsOn - tasks.formatKotlinAot - tasks.formatKotlinAotTest)
}

tasks.lintKotlin {
    setDependsOn(dependsOn - tasks.lintKotlinAot - tasks.lintKotlinAotTest)
}

graalvmNative {
    binaries.all {
        buildArgs.add("--initialize-at-build-time=kotlin.annotation.AnnotationTarget,kotlin.annotation.AnnotationRetention")
    }
}