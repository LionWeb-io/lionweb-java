plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
    alias(libs.plugins.vt.publish)
    id("integration-test-conventions")
    id("application")
    id("lionweb-publish-conventions")
}

val jvmVersion = extra["jvmVersion"] as String

java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
}

kotlin {
    jvmToolchain(jvmVersion.toInt())
}

application {
    mainClass.set("io.lionweb.server.LionWebServer")
}

tasks.register("runServerWeb", JavaExec::class) {
    description = "Run the LionWeb server with the web UI"
    mainClass.set("io.lionweb.server.LionWebServer")
    classpath = sourceSets.main.get().runtimeClasspath
    args = listOf("--web-ui")
}

tasks.register("runDemoClient1", JavaExec::class) {
    description = "Run the Demo Client 1"
    mainClass.set("io.lionweb.server.DemoClientApp")
    classpath = sourceSets.main.get().runtimeClasspath
    args = listOf("--http-port=9001", "--client-id=client1")
}

tasks.register("runDemoClient2", JavaExec::class) {
    description = "Run the Demo Client 2"
    mainClass.set("io.lionweb.server.DemoClientApp")
    classpath = sourceSets.main.get().runtimeClasspath
    args = listOf("--http-port=9002", "--client-id=client2")
}

sourceSets {
    val integrationTest by getting {
        kotlin.srcDirs("src/integrationTest/kotlin")
    }
}

val buildWebUI =
    tasks.register<Exec>("buildWebUI") {
        description = "Builds the Svelte web UI frontend"
        group = "build"
        workingDir(layout.projectDirectory.dir("web-ui"))
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        if (isWindows) {
            commandLine("cmd", "/c", "npm install && npm run build")
        } else {
            commandLine("sh", "-c", "npm install && npm run build")
        }
        inputs.dir(layout.projectDirectory.dir("web-ui/src"))
        inputs.file(layout.projectDirectory.file("web-ui/package.json"))
        inputs.file(layout.projectDirectory.file("web-ui/vite.config.ts"))
        outputs.dir(layout.projectDirectory.dir("web-ui/dist"))
    }

val copyWebUI =
    tasks.register<Copy>("copyWebUI") {
        description = "Copies the built web UI into the classpath resources"
        group = "build"
        dependsOn(buildWebUI)
        from(layout.projectDirectory.dir("web-ui/dist"))
        into(layout.buildDirectory.dir("resources/main/webui"))
    }

tasks.withType<Jar>().configureEach {
    dependsOn(copyWebUI)
}

tasks["build"].dependsOn("buildWebUI")

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("all")
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "io.lionweb.server.LionWebServer"
    }
}

mavenPublishing {
    pom {
        description.set("LionWeb Server - a standalone LionWeb repository server")
    }
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
}

dependencies {
    implementation(project(":client"))
    implementation(project(":core"))
    implementation(libs.clikt)
    implementation(libs.java.websocket)
    implementation(libs.gson)

    add("integrationTestImplementation", project(":client"))
    add("integrationTestImplementation", project(":core"))
    add("integrationTestImplementation", libs.clikt)
    add("integrationTestImplementation", libs.java.websocket)
    add("integrationTestImplementation", libs.gson)
    add("integrationTestImplementation", libs.junit.api)
    add("integrationTestRuntimeOnly", libs.junit.engine)
    add("integrationTestRuntimeOnly", libs.junit.platform.launcher)
}
