import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `jvm-test-suite`
    id("java-library")
    alias(libs.plugins.buildConfig)
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
    implementation(libs.gson)
    implementation(libs.testcontainers)
    implementation(libs.testcontainersjunit)
    implementation(libs.testcontainerspg)
}

val lionwebRepositoryCommitID = extra["lionwebRepositoryCommitID"]

buildConfig {
    sourceSets.getByName("main") {
        packageName("io.lionweb.repoclient.testing")
        buildConfigField("String", "LIONWEB_REPOSITORY_COMMIT_ID", "\"${lionwebRepositoryCommitID}\"")
        useKotlinOutput()
    }
}
