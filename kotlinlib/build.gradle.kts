plugins {
    java
    `jvm-test-suite`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
    id("java-library")
    // alias(libs.plugins.superPublish)
    alias(libs.plugins.buildConfig)
}

repositories {
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
val kotestVersion = extra["kotestVersion"]
val kotlinVersion = extra["kotlinVersion"]

dependencies {
    implementation(libs.okhttp)
    implementation(project(":core"))
    implementation(libs.gson)
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation(kotlin("test"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        register<JvmTestSuite>("functionalTest") {
            dependencies {
                implementation(project())
                // implementation(libs.kolasucore)
                implementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
                implementation("io.kotest:kotest-runner-junit5-jvm:5.8.0")
                // implementation("io.kotest.extensions:kotest-extensions-testcontainers:$kotestVersion")
                implementation("io.kotest:kotest-assertions-core:5.8.0")
                implementation("io.kotest:kotest-property:5.8.0")
                implementation("org.testcontainers:testcontainers:1.19.5")
                implementation("org.testcontainers:junit-jupiter:1.19.5")
                implementation("org.testcontainers:postgresql:1.19.5")
                implementation(project(":core"))
            }

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

// //publishing {
// //    repositories {
// //        maven {
// //            url = URI("https://maven.pkg.github.com/Strumenta/starlasu-lionweb-repository-client")
// //            credentials {
// //                username = (project.findProperty("starlasu.github.user") ?: System.getenv("starlasu_github_user")) as String?
// //                password = (project.findProperty("starlasu.github.token") ?: System.getenv("starlasu_github_token")) as String?
// //            }
// //        }
// //    }
// //}
// //
// //mavenPublishing {
// //    coordinates("com.strumenta.lwrepoclient", "lwrepoclient-base", version as String)
// //
// //    pom {
// //        name.set("lwrepoclient-base")
// //        description.set("The Kotlin client for the lionweb-repository")
// //        inceptionYear.set("2023")
// //        url.set("https://github.com/Strumenta/starlasu-lionweb-repository-client")
// //        licenses {
// //            license {
// //                name.set("The Apache License, Version 2.0")
// //                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
// //                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
// //            }
// //        }
// //        developers {
// //            developer {
// //                id.set("ftomassetti")
// //                name.set("Federico Tomassetti")
// //                url.set("https://github.com/ftomassetti/")
// //            }
// //        }
// //        scm {
// //            url.set("https://github.com/Strumenta/starlasu-lionweb-repository-client/")
// //            connection.set("scm:git:git://github.com/Strumenta/starlasu-lionweb-repository-client.git")
// //            developerConnection.set("scm:git:ssh://git@github.com/Strumenta/starlasu-lionweb-repository-client.git")
// //        }
// //    }
// //}
// //
java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = jvmVersion
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(jvmVersion.removePrefix("1.")))
    }
}

// afterEvaluate {
//    tasks {
//        named("generateMetadataFileForMavenPublication") {
//            dependsOn("kotlinSourcesJar")
//        }
//    }
// }
//
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

val lionwebRepositoryCommitID = extra["lionwebRepositoryCommitID"]

buildConfig {
    sourceSets.getByName("functionalTest") {
        packageName("com.strumenta.lwrepoclient.base")
        buildConfigField("String", "LIONWEB_REPOSITORY_COMMIT_ID", "\"${lionwebRepositoryCommitID}\"")
        useKotlinOutput()
    }
}

tasks.withType<Test> {
    testLogging {
        events("standardOut", "passed", "skipped", "failed")
    }
}
