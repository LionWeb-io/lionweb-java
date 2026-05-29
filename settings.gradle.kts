rootProject.name = "lionweb-jvm"

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version ("2.3.20")
        id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
    }
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

include("core")
include("emf")
include("emf-builtins")
include("extensions")
include("client")
include("client-testing")
include("gradle-plugin")
include("kotlin-core")
include("kotlin-client")
