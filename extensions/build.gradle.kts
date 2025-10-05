import com.vanniktech.maven.publish.SonatypeHost
import java.net.URI

plugins {
    id("java-library")
    id("signing")
    alias(libs.plugins.shadow)
    alias(libs.plugins.vtpublish)
    jacoco
    alias(libs.plugins.protobuf)
}

repositories {
    mavenCentral()
}

val jvmVersion = extra["jvmVersion"] as String
val specsVersion = extra["specsVersion"] as String

val javadocConfig by configurations.creating {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    implementation(project(":core"))
    implementation(project(":client"))
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    implementation(libs.protobuf)
    implementation(libs.gson)
    implementation(libs.okhttp)

    testImplementation(libs.junit)
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    // See https://discuss.gradle.org/t/why-subproject-sourceset-dirs-project-sourceset-dirs/7376/5
    // Without the closure, parent sources are used for children too
    from(sourceSets.getByName("main").java.srcDirs)
}

mavenPublishing {
    coordinates(
        groupId = "io.lionweb.lionweb-java",
        artifactId = "lionweb-java-${specsVersion}-" + project.name,
        version = project.version as String,
    )

    pom {
        name.set("lionweb-java-" + project.name)
        description.set("Java APIs for the LionWeb system")
        version = project.version as String
        packaging = "jar"
        url.set("https://github.com/LionWeb-io/lionweb-java")

        scm {
            connection.set("scm:git:https://github.com/LionWeb-io/lionweb-java.git")
            developerConnection.set("scm:git:git@github.com:LionWeb-io/lionweb-java.git")
            url.set("https://github.com/LionWeb-io/lionweb-java.git")
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
            developer {
                id.set("dslmeinte")
                name.set("Meinte Boersma")
                email.set("meinte.boersma@gmail.com")
            }
            developer {
                id.set("enikao")
                name.set("Niko Stotz")
                email.set("github-public@nikostotz.de")
            }
        }
    }
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, true)
    signAllPublications()
}

java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

val protobufVersion: String = libs.versions.protobufVersion.get()

protobuf {
    protoc {
        protoc {
            val arch = System.getProperty("os.arch")
            val os = System.getProperty("os.name").lowercase()
            val classifier = if (os.contains("mac") && arch == "aarch64") "osx-aarch_64" else ""

            artifact = if (classifier.isNotEmpty())
                "com.google.protobuf:protoc:4.27.2:$classifier"
            else
                "com.google.protobuf:protoc:4.27.2"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
        }
    }
}

tasks {
    getByName("sourcesJar").dependsOn("generateProto")
}

tasks.withType<Test>().all {
    testLogging {
        showStandardStreams = true
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    // Set the environment variable so that Testcontainers can reuse containers between test runs
    environment("TESTCONTAINERS_REUSE_ENABLE", "true")
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
    "functionalTestImplementation"(project(":core"))
    "functionalTestImplementation"(project(":extensions"))
    "functionalTestImplementation"(project(":client"))
    "functionalTestImplementation"(project(":client-testing"))
    "functionalTestImplementation"(libs.testcontainers)
    "functionalTestImplementation"(libs.testcontainersjunit)
    "functionalTestImplementation"(libs.testcontainerspg)
    "functionalTestImplementation"(libs.junit.api)
    "functionalTestImplementation"(libs.junit.engine)
    "functionalTestImplementation"(libs.gson)
}
