plugins {
    id("java-library")
    alias(libs.plugins.vt.publish)
    id("lionweb-publish-conventions")
}

repositories {
    mavenCentral()
}

val javadocConfig by configurations.creating {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    api(project(":core"))
    api(project(":emf-builtins"))

    api(libs.emf.common)
    api(libs.emf.ecore)
    api(libs.emf.ecore.xmi)

    implementation(libs.emfjson)

    implementation(libs.gson)
    implementation(libs.annotations)
    compileOnly(libs.jsr305)

    // Use JUnit test framework.
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.gson)
}

val jvmVersion = extra["jvmVersion"] as String

java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
}
tasks.register<Javadoc>("myJavadoc") {
    source = sourceSets.main.get().allJava
    classpath = javadocConfig
    options {
        require(this is StandardJavadocDocletOptions)
        addStringOption("link", "https://docs.oracle.com/javase/8/docs/api/")
        addStringOption("link", "https://download.eclipse.org/modeling/emf/emf/javadoc/2.10.0/")
        addStringOption("link", "https://alexanderpann.github.io/mps-openapi-doc/javadoc_2021.2/")
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
    pom {
        description.set("EMF compatibility layer for LionWeb")
        developers {
            developer {
                id.set("Ulyana-F1re")
                name.set("Ulyana Tikhonova")
                url.set("https://github.com/Ulyana-F1re")
            }
        }
    }
}
