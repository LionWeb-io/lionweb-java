plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    id("integration-test-conventions")
    id("application")
}

val jvmVersion = extra["jvmVersion"] as String

java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
}

kotlin {
    jvmToolchain(jvmVersion.toInt())
}

application {
    mainClass.set("io.lionweb.server.LionWebServerKt")
}

tasks.withType<Test>().all {
    testLogging {
        showStandardStreams = true
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

sourceSets {
    val integrationTest by getting {
        kotlin.srcDirs("src/integrationTest/kotlin")
    }
}

dependencies {
    implementation(project(":client"))
    implementation(project(":core"))
    implementation(libs.clikt)
    implementation(libs.java.websocket)
    implementation(libs.gson)

    add("integrationTestImplementation", project(":client"))
    add("integrationTestImplementation", project(":core"))
    add("integrationTestImplementation", libs.clikt)
    add("integrationTestImplementation", libs.java.websocket)
    add("integrationTestImplementation", libs.gson)
    add("integrationTestImplementation", libs.junit.api)
    add("integrationTestRuntimeOnly", libs.junit.engine)
    add("integrationTestRuntimeOnly", libs.junit.platform.launcher)
}
