import com.vanniktech.maven.publish.SonatypeHost
import java.util.jar.Manifest

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
    implementation(libs.lwjavacore)
    implementation(libs.lwjavarepo)
    implementation(libs.lwjavaextensions)
    implementation(libs.gson)
    implementation(libs.kotlinreflect)
    testImplementation(kotlin("test"))
    implementation(libs.protobuf)

    "functionalTestImplementation"(project(":repo-client"))
    "functionalTestImplementation"(project(":core"))
    "functionalTestImplementation"(libs.lwjavacore)
    "functionalTestImplementation"(libs.lwjavaextensions)
    "functionalTestImplementation"(libs.ktestjunit)
    "functionalTestImplementation"(libs.kotestrunner)
    "functionalTestImplementation"(libs.kotestassertions)
    "functionalTestImplementation"(libs.kotestproperty)
    "functionalTestImplementation"(libs.testcontainers)
    "functionalTestImplementation"(libs.testcontainersjunit)
    "functionalTestImplementation"(libs.testcontainerspg)
    "functionalTestImplementation"(libs.lwjavarepo)
    "functionalTestImplementation"(libs.lwjavarepotesting)
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

val lwJavaJar =
    configurations.findByName("functionalTestRuntimeClasspath")!!
        .find { it.name.startsWith("lionweb-java-2024.1-repo-client-0") } as File
// Eventually we should use this value and drop it from gradle.properties
val lwJavaLionwebRepositoryCommitID: String? =
    zipTree(lwJavaJar).matching {
        include("META-INF/MANIFEST.MF")
    }.files.firstOrNull()?.let { manifestFile ->
        manifestFile.inputStream().use {
            Manifest(it)
        }.mainAttributes.getValue("lionwebRepositoryCommitID")
    }

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
