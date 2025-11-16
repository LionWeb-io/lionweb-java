plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.2.0"
}

project.group = "io.lionweb"

gradlePlugin {
    website.set("https://github.com/lionweb/lionweb-java")
    vcsUrl.set("https://github.com/lionweb/lionweb-java.git")
    plugins {
        create("lwGradlePlugin") {
            id = "io.lionweb"
            displayName = "LionWeb Gradle Plugin"
            description = "LionWeb Gradle Plugin"
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

