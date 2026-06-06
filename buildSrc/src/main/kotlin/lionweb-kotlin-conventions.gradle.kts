import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("lionweb-java-conventions")
    kotlin("jvm")
}

val jvmVersion = extra["jvmVersion"] as String

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(jvmVersion))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(jvmVersion.toInt()))
    }
}

// Extend the sourcesJar registered by lionweb-java-conventions to also include Kotlin sources
val kotlinMain = the<KotlinJvmProjectExtension>().sourceSets.getByName("main")
tasks.named<Jar>("sourcesJar") {
    from(kotlinMain.kotlin.srcDirs)
}
