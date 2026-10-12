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

// Since JDK 23 the javadoc tool bundles ~4 MB of web fonts into every generated site, which bloats
// the -javadoc.jar files published to Maven Central. Skip them when the javadoc tool supports it.
tasks.withType<Javadoc>().configureEach {
    doFirst {
        if (javadocTool.get().metadata.languageVersion.asInt() >= 23) {
            (options as StandardJavadocDocletOptions).addBooleanOption("-no-fonts", true)
        }
    }
}
