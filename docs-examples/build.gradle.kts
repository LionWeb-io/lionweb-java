plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}


dependencies {
    implementation(project(":core"))
    implementation(libs.gson)
}

val jvmVersion = extra["jvmVersion"] as String

java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
}
