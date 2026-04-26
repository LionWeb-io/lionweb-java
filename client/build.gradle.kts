plugins {
    `jvm-test-suite`
    id("java-library")
    id("signing")
    alias(libs.plugins.shadow)
    alias(libs.plugins.vt.publish)
    jacoco
}

repositories {
    mavenCentral()
}

val lionwebServerCommitID: String by project

val jvmVersion = extra["jvmVersion"] as String
val specsVersion = extra["specsVersion"] as String

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["lionwebServerCommitID"] = lionwebServerCommitID
    }
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    // See https://discuss.gradle.org/t/why-subproject-sourceset-dirs-project-sourceset-dirs/7376/5
    // Without the closure, parent sources are used for children too
    from(sourceSets.getByName("main").java.srcDirs)
}

mavenPublishing {
    coordinates(
        groupId = "io.lionweb",
        artifactId = "lionweb-$specsVersion-" + project.name,
        version = project.version as String,
    )

    pom {
        name.set("lionweb-" + project.name)
        description.set("Java APIs for the LionWeb system")
        version = project.version as String
        packaging = "jar"
        url.set("https://github.com/LionWeb-io/lionweb-jvm")

        scm {
            connection.set("scm:git:https://github.com/LionWeb-io/lionweb-jvm.git")
            developerConnection.set("scm:git:git@github.com:LionWeb-io/lionweb-jvm.git")
            url.set("https://github.com/LionWeb-io/lionweb-jvm.git")
        }

        licenses {
            license {
                name.set("Apache License V2.0")
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
    publishToMavenCentral(true)
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

val performanceTestSourceSet = sourceSets.create("performanceTest") {
    compileClasspath += sourceSets["main"].output + sourceSets["test"].output
    runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
}

configurations["performanceTestImplementation"]
    .extendsFrom(configurations["testImplementation"])
configurations["performanceTestRuntimeOnly"]
    .extendsFrom(configurations["testRuntimeOnly"])

tasks.register<Test>("performanceTest") {
    group = "Verification"
    description = "Runs performance tests in src/performanceTest/java"
    shouldRunAfter(tasks.test)
    testClassesDirs = performanceTestSourceSet.output.classesDirs
    classpath = performanceTestSourceSet.runtimeClasspath
    useJUnitPlatform()
    testLogging {
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
        )
        showStandardStreams = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.caffeine)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    add("performanceTestImplementation", "org.openjdk.jmh:jmh-core:1.37")
    add("performanceTestAnnotationProcessor", "org.openjdk.jmh:jmh-generator-annprocess:1.37")

    "functionalTestImplementation"(project(":core"))
    "functionalTestImplementation"(project(":client"))
    "functionalTestImplementation"(project(":client-testing"))
    "functionalTestImplementation"(libs.testcontainers)
    "functionalTestImplementation"(libs.testcontainers.junit)
    "functionalTestImplementation"(libs.testcontainers.pg)
    "functionalTestImplementation"(libs.junit.api)
    "functionalTestImplementation"(libs.junit.engine)
    "functionalTestRuntimeOnly"(libs.junit.platform.launcher)
    "functionalTestImplementation"(libs.gson)
}
