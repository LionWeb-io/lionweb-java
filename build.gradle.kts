plugins {
    alias(libs.plugins.release)
    alias(libs.plugins.superPublish) apply(false)
    alias(libs.plugins.kotlinJvm) apply(false)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.dokka) apply(false)
}

repositories {
    mavenCentral()
}

allprojects {

    group = "io.lionweb.lionweb-kotlin"
    project.version = version

    repositories {
        mavenLocal()
        mavenCentral()

        // OSSRH (Old Default)
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            mavenContent { snapshotsOnly() } // Ensures only snapshots are fetched
        }

        // Newer Sonatype OSSRH Instance (s01)
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            mavenContent { snapshotsOnly() }
        }
    }
}

release {
    buildTasks =
        listOf(
            ":core:publishAllPublicationsToMavenCentralRepository",
            ":repo-client:publishAllPublicationsToMavenCentralRepository",
        )
    versionPropertyFile = "./gradle.properties"
    git {
        requireBranch.set("main")
        pushToRemote.set("origin")
    }
}

tasks.wrapper {
    gradleVersion = "8.13"
    // You can either download the binary-only version of Gradle (BIN) or
    // the full version (with sources and documentation) of Gradle (ALL)
    distributionType = Wrapper.DistributionType.ALL
}
