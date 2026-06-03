plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.plugins.vt.publish.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}" })
}
