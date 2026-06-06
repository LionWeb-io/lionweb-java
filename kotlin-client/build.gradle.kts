plugins {
    `jvm-test-suite`
    id("lionweb-kotlin-conventions")
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
    id("java-library")
    alias(libs.plugins.vt.publish)
    id("lionweb-publish-conventions")
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
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
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
