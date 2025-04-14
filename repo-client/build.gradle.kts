import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `jvm-test-suite`
    id("java-library")
    id("signing")
    id("com.github.johnrengelman.shadow") version "7.1.1"
    id("com.vanniktech.maven.publish")
    jacoco
}

repositories {
    mavenCentral()
}

val lionwebRepositoryCommitID: String by project

val jvmVersion = extra["jvmVersion"] as String
val specsVersion = extra["specsVersion"] as String

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["lionwebRepositoryCommitID"] = lionwebRepositoryCommitID
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":extensions"))
    implementation(libs.okhttp)
    implementation(libs.gson)
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
        artifactId = "lionweb-java-$specsVersion-" + project.name,
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
    publishToMavenCentral(SonatypeHost.S01, true)
    signAllPublications()
}

java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

tasks.withType<Test>().all {
    testLogging {
        showStandardStreams = true
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
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
    "functionalTestImplementation"(project(":repo-client"))
    "functionalTestImplementation"(project(":core"))
    "functionalTestImplementation"(project(":repo-client-testing"))
    "functionalTestImplementation"(libs.testcontainers)
    "functionalTestImplementation"(libs.testcontainersjunit)
    "functionalTestImplementation"(libs.testcontainerspg)
    "functionalTestImplementation"("org.junit.jupiter:junit-jupiter-api:5.8.1")
    "functionalTestImplementation"("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    "functionalTestImplementation"(libs.gson)
}
