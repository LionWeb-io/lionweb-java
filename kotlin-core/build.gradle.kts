plugins {
    `jvm-test-suite`
    id("lionweb-kotlin-conventions")
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
    id("java-library")
    alias(libs.plugins.vt.publish)
    id("lionweb-publish-conventions")
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

dependencies {
    implementation(libs.okhttp)
    implementation(project(":core"))
    implementation(libs.gson)
    implementation(libs.kotlin.reflect)
    testImplementation(libs.ktest.junit)
}

mavenPublishing {
    pom {
        description.set("Bindings to facilitate usage of LionWeb Java from Kotlin")
    }
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
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
