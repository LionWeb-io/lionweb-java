plugins {
    id("lionweb-kotlin-conventions")
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
    id("application")
}

application {
    mainClass.set("io.lionweb.emf.cli.LionWebEMFCli")
    applicationName = "lionweb-emf"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":emf"))
    implementation(libs.clikt)
    implementation(libs.gson)

    testImplementation(libs.ktest.junit)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Implementation-Version"] = project.version
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("lionweb-emf-cli")
    archiveClassifier.set("all")
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "io.lionweb.emf.cli.LionWebEMFCli"
    }
}
