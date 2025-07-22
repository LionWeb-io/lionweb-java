plugins {
    `jvm-test-suite`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
    id("java-library")
    alias(libs.plugins.vtPublish)
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

dependencies {
    implementation(libs.okhttp)
    implementation(libs.lwjavacore)
    implementation(libs.gson)
    implementation(libs.kotlinreflect)
    testImplementation(libs.ktestjunit)
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
        description.set("Bindings to facilitate usage of LionWeb Java from Kotlin")
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

tasks.withType<Test> {
    testLogging {
        events("standardOut", "passed", "skipped", "failed")
    }
}
