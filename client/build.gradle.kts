import java.util.jar.Manifest

plugins {
    `jvm-test-suite`

    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
    id("java-library")
    alias(libs.plugins.vt.publish)
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
    implementation(libs.lwjava.core)
    implementation(libs.lwjava.client)
    implementation(libs.lwjava.extensions)
    implementation(libs.gson)
    implementation(libs.kotlin.reflect)
    testImplementation(kotlin("test"))
    implementation(libs.protobuf)

    "functionalTestImplementation"(project(":core"))
    "functionalTestImplementation"(project(":client"))
    "functionalTestImplementation"(libs.lwjava.core)
    "functionalTestImplementation"(libs.lwjava.extensions)
    "functionalTestImplementation"(libs.ktest.junit)
    "functionalTestImplementation"(libs.kotest.runner)
    "functionalTestImplementation"(libs.kotest.assertions)
    "functionalTestImplementation"(libs.kotest.property)
    "functionalTestImplementation"(libs.testcontainers)
    "functionalTestImplementation"(libs.testcontainers.junit)
    "functionalTestImplementation"(libs.testcontainers.pg)
    "functionalTestImplementation"(libs.lwjava.client)
    "functionalTestImplementation"(libs.lwjava.client.testing)
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
    publishToMavenCentral(true)
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

val lwJavaJar =
    configurations
        .findByName("functionalTestRuntimeClasspath")!!
        .find { it.name.startsWith("lionweb-java-2024.1-client-1") } as File
// Eventually we should use this value and drop it from gradle.properties
val lwJavaLionwebRepositoryCommitID: String? =
    zipTree(lwJavaJar)
        .matching {
            include("META-INF/MANIFEST.MF")
        }.files
        .firstOrNull()
        ?.let { manifestFile ->
            manifestFile
                .inputStream()
                .use {
                    Manifest(it)
                }.mainAttributes
                .getValue("lionwebRepositoryCommitID")
        }

tasks.withType<Test> {
    testLogging {
        events("standardOut", "passed", "skipped", "failed")
    }
}
