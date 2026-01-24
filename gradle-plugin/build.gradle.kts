import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.2.0"
    alias(libs.plugins.vtpublish)
}

project.group = "io.lionweb"

gradlePlugin {
    website.set("https://github.com/LionWeb-io/LionWeb-java")
    vcsUrl.set("https://github.com/LionWeb-io/lionweb-java.git")
    plugins {
        create("lwGradlePlugin") {
            id = "io.lionweb"
            displayName = "LionWeb Gradle Plugin"
            description = "Generate Java classes for a given LionWeb Language definition"
            tags.set(listOf("lionweb", "language engineering", "MDE"))
            implementationClass = "io.lionweb.gradleplugin.LionWebPlugin"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(libs.gson)
    implementation(libs.protobuf)
    implementation(libs.javapoet)
    implementation("org.jetbrains:annotations:17.0.0")
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// In order to use JavaPoet, we cannot stick to Java 8
java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}

val specsVersion = extra["specsVersion"] as String
