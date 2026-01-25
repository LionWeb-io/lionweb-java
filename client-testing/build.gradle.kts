plugins {
    `jvm-test-suite`
    id("java-library")
    alias(libs.plugins.build.config)
    id("signing")
    alias(libs.plugins.vt.publish)
}

val jvmVersion = extra["jvmVersion"] as String
val specsVersion = extra["specsVersion"] as String

java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
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

dependencies {
    implementation(libs.okhttp)
    implementation(project(":core"))
    implementation(project(":client"))
    implementation(libs.gson)
    implementation(libs.testcontainers)
    implementation(libs.testcontainers.junit)
    implementation(libs.testcontainers.pg)
    implementation(libs.junit.api)
    runtimeOnly(libs.junit.engine)
}

val lionwebServerCommitID = extra["lionwebServerCommitID"]

buildConfig {
    sourceSets.getByName("main") {
        packageName("io.lionweb.client.testing")
        buildConfigField("String", "LIONWEB_SERVER_COMMIT_ID", "\"${lionwebServerCommitID}\"")
        useJavaOutput()
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
        artifactId = "lionweb-${specsVersion}-" + project.name,
        version = project.version as String,
    )

    pom {
        name.set("lionweb-java-" + project.name)
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

tasks.named("sourcesJar") {
    dependsOn("generateBuildConfig")
}