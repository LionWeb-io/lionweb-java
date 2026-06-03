plugins {
    `jvm-test-suite`
    id("java-library")
    alias(libs.plugins.shadow)
    alias(libs.plugins.vt.publish)
    jacoco
    id("integration-test-conventions")
    id("lionweb-publish-conventions")
}

repositories {
    mavenCentral()
}

val lionwebServerCommitID: String by project

val jvmVersion = extra["jvmVersion"] as String

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
    pom {
        description.set("Java APIs for the LionWeb system")
    }
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

tasks.named<Test>("integrationTest") {
    dependsOn(project(":core").tasks.named("downloadIntegrationTestResources"))
    description = "Runs delta protocol integration tests against the lionweb-integration-testing examples"
    val deltaDir =
        File(
            project(":core").layout.buildDirectory.get().asFile,
            "integrationTestResources/delta")
    environment("deltaIntegrationTestingDir", deltaDir.absolutePath)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.okhttp)
    implementation(libs.gson)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    add("integrationTestImplementation", libs.junit.api)
    add("integrationTestRuntimeOnly", libs.junit.engine)
    add("integrationTestRuntimeOnly", libs.junit.platform.launcher)
    add("performanceTestImplementation", libs.jmh.core)
    add("performanceTestAnnotationProcessor", libs.jmh.annprocess)

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
