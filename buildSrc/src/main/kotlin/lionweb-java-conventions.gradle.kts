plugins {
    java
}

val jvmVersion = extra["jvmVersion"] as String

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("standardOut", "passed", "skipped", "failed")
    }
}
