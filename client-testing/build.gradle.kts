plugins {
    `jvm-test-suite`
    id("java-library")
    alias(libs.plugins.build.config)
    alias(libs.plugins.vt.publish)
    id("lionweb-publish-conventions")
    id("lionweb-java-conventions")
}

repositories {
    mavenLocal()
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
    runtimeOnly(libs.junit.platform.launcher)
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
    pom {
        description.set("Java APIs for the LionWeb system")
    }
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
}

tasks.named("sourcesJar") {
    dependsOn("generateBuildConfigClasses")
}
