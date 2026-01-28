import java.net.URI
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    id("java-library")
    id("signing")
    alias(libs.plugins.shadow)
    alias(libs.plugins.vt.publish)
    jacoco
    alias(libs.plugins.protobuf)
}

repositories {
    mavenCentral()
    // Required for MPS OpenAPI and Modelix's Model API
    maven {
        url = URI("https://artifacts.itemis.cloud/repository/maven-mps/")
    }
}

val jvmVersion = extra["jvmVersion"] as String
val specsVersion = extra["specsVersion"] as String

val javadocConfig by configurations.creating {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    // Use JUnit test framework.
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api(libs.commonsMath3)

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(libs.guava)

    javadocConfig(libs.emf.ecore)

    // Please note that this forces us to use Java 11 for the javadoc tasks
    // unfortunately earlier version of these libraries, which were compatible with Java 8, are not available
    // on Maven
    javadocConfig(libs.mpsOpenApi)
    javadocConfig(libs.modelApi)
    implementation(libs.gson)
    implementation(libs.jsonSchemaValidator)
    implementation(libs.protobuf)
}

tasks.withType<Javadoc>().configureEach {
    val mainSourceSet = sourceSets.main.get()
    classpath = files(mainSourceSet.output, mainSourceSet.compileClasspath, javadocConfig)
    (options as StandardJavadocDocletOptions).apply {
        links(
            "https://docs.oracle.com/javase/8/docs/api/",
            "https://download.eclipse.org/modeling/emf/emf/javadoc/2.10.0/",
            "https://alexanderpann.github.io/mps-openapi-doc/javadoc_2021.2/",
        )
        addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.register<Javadoc>("myJavadoc") {
    source = sourceSets.main.get().allJava
}

tasks.register<Jar>("javadocJar") {
    dependsOn("myJavadoc")
    from(tasks.getByName("myJavadoc")/*.destinationDir*/)
    archiveClassifier.set("javadoc")
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
        artifactId = "lionweb-${specsVersion}-" + project.name,
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
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
}

java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
}

val integrationTestSourceSet = sourceSets.create("integrationTest") {
    compileClasspath += sourceSets["main"].output
    runtimeClasspath += sourceSets["main"].output
}

configurations["integrationTestImplementation"]
    .extendsFrom(configurations["testImplementation"])
configurations["integrationTestRuntimeOnly"]
    .extendsFrom(configurations["testRuntimeOnly"])

val integrationTestResources : File = File(project.buildDir, "integrationTestResources")

val downloadIntegrationTestResources = tasks.register("downloadIntegrationTestResources") {
    val repoURL = "https://github.com/LionWeb-io/lionweb-integration-testing.git"
    doLast {
        val destinationDir = integrationTestResources
        if (destinationDir.exists()) {
            logger.info("Not downloading integration test resources as directory ${destinationDir.absolutePath} exist")
        } else {
            val cmdLine = "git clone --depth 1 $repoURL ${destinationDir.absolutePath}"
            logger.info("About to download integration test using this command: $cmdLine")
            val process = ProcessBuilder()
                .command(cmdLine.split(" "))
                .directory(project.buildDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
            val finished = process.waitFor(60, TimeUnit.SECONDS)
            if (logger.isInfoEnabled) {
                logger.info("--- Git process output - start ---")
                logger.info(process.inputStream.reader().readText())
                logger.info("--- Git process output - end ---")
            }
            if (!finished) {
                throw RuntimeException("Unable to download the repository $repoURL in 60 seconds, giving up")
            } else {
                logger.info("Downloaded integration test resources repo from $repoURL in directory ${destinationDir.absolutePath}")
            }
        }
        require(destinationDir.exists() && destinationDir.isDirectory) {
            throw IllegalStateException("Directory $destinationDir does not exist or is not a directory")
        }
    }
}

// Add a task to run the integration tests
val integrationTest = tasks.create("integrationTest", Test::class.java) {
    dependsOn(downloadIntegrationTestResources)
    group = "Verification"
    description = "Runs integration tests in core/src/integrationTest"
    shouldRunAfter(tasks.test)
    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    environment("integrationTestingDir", File(integrationTestResources.absolutePath, "testset"))

    useJUnitPlatform()

    testLogging {
        events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }

    val executedClasses = mutableSetOf<String>()
    addTestListener(
        object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) {}
            override fun beforeTest(testDescriptor: TestDescriptor) {}
            override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
                testDescriptor.className?.let { executedClasses.add(it) }
            }

            override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                if (suite.parent == null) {
                    println(
                        "Integration tests: ${result.testCount} invocations " +
                            "from ${executedClasses.size} classes; " +
                            "${result.successfulTestCount} passed, " +
                            "${result.failedTestCount} failed, " +
                            "${result.skippedTestCount} skipped"
                    )
                }
            }
        },
    )
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // run tests before generating report
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

protobuf {
    protoc {
        protoc {
            val arch = System.getProperty("os.arch")
            val os = System.getProperty("os.name").lowercase()
            val classifier = if (os.contains("mac") && arch == "aarch64") "osx-aarch_64" else ""

            val protocVersion = libs.versions.protobufVersion.get()
            artifact = if (classifier.isNotEmpty())
                "com.google.protobuf:protoc:$protocVersion:$classifier"
            else
                "com.google.protobuf:protoc:$protocVersion"
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

sourceSets {
    create("experiments") {
        compileClasspath += sourceSets.main.get().output
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // run report after tests
    useJUnitPlatform {
        val includeExpensive = project.findProperty("includeExpensiveTests") == "true"
        if (!includeExpensive) {
            excludeTags("performance")
        }
    }
}
