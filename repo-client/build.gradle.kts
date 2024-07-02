import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `jvm-test-suite`

    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
    id("java-library")
    alias(libs.plugins.superPublish)
    alias(libs.plugins.kotlinJvm)
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

dependencies {
    implementation(libs.okhttp)
    implementation(project(":core"))
    implementation(libs.lwjavacore)
    implementation(libs.gson)
    implementation(libs.kotlinreflect)
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
                implementation(project(":core"))
                implementation(libs.lwjavacore)
                implementation(libs.ktestjunit)
                implementation("io.kotest:kotest-runner-junit5-jvm:5.8.0")
                implementation("io.kotest:kotest-assertions-core:5.8.0")
                implementation("io.kotest:kotest-property:5.8.0")
                implementation("org.testcontainers:testcontainers:1.19.5")
                implementation("org.testcontainers:junit-jupiter:1.19.5")
                implementation("org.testcontainers:postgresql:1.19.5")
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

mavenPublishing {
    coordinates("com.strumenta.lwrepoclient", "lwrepoclient-base", version as String)

    pom {
        name.set("lionweb-kotlin-" + project.name)
        description.set("The Kotlin client for the lionweb-repository")
        inceptionYear.set("2023")
        url.set("https://github.com/LionWeb-io/lionweb-kotlin")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("ftomassetti")
                name.set("Federico Tomassetti")
                url.set("https://github.com/ftomassetti/")
            }
        }
        scm {
            connection.set("scm:git:https://github.com/LionWeb-io/lionweb-kotlin.git")
            developerConnection.set("scm:git:git@github.com:LionWeb-io/lionweb-kotlin.git")
            url.set("https://github.com/LionWeb-io/lionweb-kotlin.git")
        }
    }
}

val specsVersion: String by project

mavenPublishing {
    coordinates(
        groupId = "io.lionweb.lionweb-kotlin",
        artifactId = "lionweb-kotlin-$specsVersion-" + project.name,
        version = project.version as String,
    )

    pom {
        name.set("lionweb-kotlin-" + project.name)
        description.set("Client library to connect to the LionWeb Repository")
        version = project.version as String
        packaging = "jar"
        url.set("https://github.com/LionWeb-io/lionweb-kotlin")

        scm {
            connection.set("scm:git:https://github.com/LionWeb-io/lionweb-kotlin.git")
            developerConnection.set("scm:git:git@github.com:LionWeb-io/lionweb-kotlin.git")
            url.set("https://github.com/LionWeb-io/lionweb-kotlin.git")
        }

        licenses {
            license {
                name.set("Apache Licenve V2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                distribution.set("repo")
            }
        }

        // The developers entry is strictly required by Maven Central
        developers {
            developer {
                id.set("ftomassetti")
                name.set("Federico Tomassetti")
                email.set("federico@strumenta.com")
            }
        }
    }
    publishToMavenCentral(SonatypeHost.S01, true)
    signAllPublications()
}

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

val lionwebRepositoryCommitID = extra["lionwebRepositoryCommitID"]

buildConfig {
    sourceSets.getByName("functionalTest") {
        packageName("io.lionweb.lioncore.kotlin.repoclient")
        buildConfigField("String", "LIONWEB_REPOSITORY_COMMIT_ID", "\"${lionwebRepositoryCommitID}\"")
        useKotlinOutput()
    }
}

tasks.withType<Test> {
    testLogging {
        events("standardOut", "passed", "skipped", "failed")
    }
}
