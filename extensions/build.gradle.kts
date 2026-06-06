plugins {
    id("java-library")
    alias(libs.plugins.shadow)
    alias(libs.plugins.vt.publish)
    jacoco
    id("lionweb-functional-test-conventions")
    id("lionweb-publish-conventions")
    id("lionweb-java-conventions")
}

val javadocConfig by configurations.creating {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    implementation(project(":core"))
    implementation(project(":client"))
    compileOnly(libs.jsr305)

    implementation(libs.protobuf)
    implementation(libs.gson)
    implementation(libs.okhttp)

    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.api)
}

mavenPublishing {
    pom {
        description.set("Java APIs for the LionWeb system")
    }
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

tasks.withType<Test>().all {
    // Set the environment variable so that Testcontainers can reuse containers between test runs
    environment("TESTCONTAINERS_REUSE_ENABLE", "true")
}

dependencies {
    "functionalTestImplementation"(project(":core"))
    "functionalTestImplementation"(project(":extensions"))
    "functionalTestImplementation"(project(":client"))
    "functionalTestImplementation"(project(":client-testing"))
    "functionalTestImplementation"(libs.testcontainers)
    "functionalTestImplementation"(libs.testcontainers.junit)
    "functionalTestImplementation"(libs.testcontainers.pg)
    "functionalTestImplementation"(libs.junit.api)
    "functionalTestImplementation"(libs.junit.engine)
    "functionalTestRuntimeOnly"(libs.junit.platform.launcher)
    "functionalTestImplementation"(libs.gson)
}
