plugins {
    alias(libs.plugins.release)
    alias(libs.plugins.vt.publish) apply (false)
    alias(libs.plugins.kotlin.jvm) apply (false)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.dokka) apply (false)
    alias(libs.plugins.versioncheck)
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
    }
}

release {
    buildTasks =
        listOf(
            ":core:publishAllPublicationsToMavenCentralRepository",
            ":client:publishAllPublicationsToMavenCentralRepository",
        )
    versionPropertyFile = "./gradle.properties"
    git {
        requireBranch.set("main")
        pushToRemote.set("origin")
    }
}

tasks.wrapper {
    gradleVersion = "8.14.3"
    // You can either download the binary-only version of Gradle (BIN) or
    // the full version (with sources and documentation) of Gradle (ALL)
    distributionType = Wrapper.DistributionType.ALL
}
