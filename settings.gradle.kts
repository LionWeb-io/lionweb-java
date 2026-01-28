rootProject.name = "lionweb-jvm"

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version ("2.0.21")
        id("org.gradle.toolchains.foojay-resolver-convention") version ("0.7.0")
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
include("docs-examples")
include("gradle-plugin")
include("kotlin-core")
include("kotlin-client")
