import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `jvm-test-suite`
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
    id("java-library")
    alias(libs.plugins.vt.publish)
    id("lionweb-publish-conventions")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.build.config)
}

repositories {
    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
    mavenLocal()
    mavenCentral()
}

tasks.withType<Test>().all {
    testLogging {
        showStandardStreams = true
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

ktlint {
    filter {
        exclude { element ->
            element
                .file
                .absolutePath
                .split(File.separator)
                .contains("build")
        }
    }
}

val jvmVersion = extra["jvmVersion"] as String

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        register<JvmTestSuite>("functionalTest") {

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

dependencies {
    implementation(libs.okhttp)
    implementation(project(":core"))
    implementation(project(":kotlin-core"))
    implementation(project(":client"))
    implementation(project(":extensions"))
    implementation(libs.gson)
    implementation(libs.kotlin.reflect)
    testImplementation(kotlin("test"))
    implementation(libs.protobuf)

    "functionalTestImplementation"(project(":kotlin-core"))
    "functionalTestImplementation"(project(":kotlin-client"))
    "functionalTestImplementation"(project(":core"))
    "functionalTestImplementation"(project(":extensions"))
    "functionalTestImplementation"(libs.ktest.junit)
    "functionalTestImplementation"(libs.kotest.runner)
    "functionalTestImplementation"(libs.kotest.assertions)
    "functionalTestImplementation"(libs.kotest.property)
    "functionalTestImplementation"(libs.testcontainers)
    "functionalTestImplementation"(libs.testcontainers.junit)
    "functionalTestImplementation"(libs.testcontainers.pg)
    "functionalTestImplementation"(project(":client"))
    "functionalTestImplementation"(project(":client-testing"))
}

mavenPublishing {
    pom {
        description.set("Client library to connect to the LionWeb Repository")
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(jvmVersion))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(jvmVersion.removePrefix("1.")))
    }
}

afterEvaluate {
    tasks {
        named("generateMetadataFileForMavenPublication") {
            dependsOn("kotlinSourcesJar")
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<Test> {
    testLogging {
        events("standardOut", "passed", "skipped", "failed")
    }
}
