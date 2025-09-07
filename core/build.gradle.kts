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
    testImplementation(libs.junit)

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api(libs.commonsMath3)

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(libs.guava)

    javadocConfig(emf.ecore)

    // Please note that this forces us to use Java 11 for the javadoc tasks
    // unfortunately earlier version of these libraries, which were compatible with Java 8, are not available
    // on Maven
    javadocConfig(libs.mpsOpenApi)
    javadocConfig(libs.modelApi)
    implementation(libs.gson)
    implementation(libs.jsonSchemaValidator)
    implementation(libs.protobuf)
}

tasks.register<Javadoc>("myJavadoc") {
    source = sourceSets.main.get().allJava
    classpath = javadocConfig
    options {
        require(this is StandardJavadocDocletOptions)
        addStringOption("link", "https://docs.oracle.com/javase/8/docs/api/")
        addStringOption("link", "https://download.eclipse.org/modeling/emf/emf/javadoc/2.10.0/")
        addStringOption("link", "https://alexanderpann.github.io/mps-openapi-doc/javadoc_2021.2/")
        addStringOption("Xdoclint:none", "-quiet")
    }
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

val integrationTestSourceSet = sourceSets.create("integrationTest") {
    compileClasspath += sourceSets["main"].output
    runtimeClasspath += sourceSets["main"].output
}

configurations["integrationTestImplementation"]
    .extendsFrom(configurations["testImplementation"])

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
    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    environment("integrationTestingDir", File(integrationTestResources.absolutePath, "testset"))
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
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // run report after tests
    useJUnit {
        // This cast enables access to includeCategories and excludeCategories
        this as JUnitOptions

        val includeExpensive = project.findProperty("includeExpensiveTests") == "true"
        if (!includeExpensive) {
            excludeCategories("io.lionweb.PerformanceTest")
        }
    }
}
