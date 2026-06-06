plugins {
    java
}

val jvmVersion = extra["jvmVersion"] as String

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    // See https://discuss.gradle.org/t/why-subproject-sourceset-dirs-project-sourceset-dirs/7376/5
    // Without the closure, parent sources are used for children too
    from(sourceSets.getByName("main").java.srcDirs)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("standardOut", "passed", "skipped", "failed")
    }
}
