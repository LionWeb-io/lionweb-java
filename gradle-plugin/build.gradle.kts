plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.2.0"
    alias(libs.plugins.vt.publish)
    alias(libs.plugins.build.config)
}

project.group = "io.lionweb"

gradlePlugin {
    website.set("https://github.com/LionWeb-io/lionweb-jvm")
    vcsUrl.set("https://github.com/LionWeb-io/lionweb-jvm.git")
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

val integrationTest by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output + configurations["testRuntimeClasspath"]
    runtimeClasspath += output + compileClasspath
}

configurations["integrationTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    implementation(project(":core"))
    implementation(libs.gson)
    implementation(libs.protobuf)
    implementation(libs.javapoet)
    implementation("org.jetbrains:annotations:17.0.0")
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    "integrationTestImplementation"(gradleTestKit())
    "integrationTestImplementation"("org.junit.jupiter:junit-jupiter:5.7.1")
    "integrationTestImplementation"("com.google.code.findbugs:jsr305:3.0.2")
    "integrationTestRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests against the full plugin pipeline."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    mustRunAfter(tasks.test)
}

// In order to use JavaPoet, we cannot stick to Java 8
java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}

val specsVersion = extra["specsVersion"] as String

buildConfig {
    sourceSets.getByName("main") {
        packageName("io.lionweb.gradleplugin")
        buildConfigField("String", "VERSION", "\"${version}\"")
        useJavaOutput()
    }
}