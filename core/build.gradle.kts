import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URI

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("com.github.johnrengelman.shadow") version "7.1.1"
}

repositories {
    mavenCentral()
    // Required for MPS OpenAPI and Modelix"s Model API
    maven {
        url = URI("https://artifacts.itemis.cloud/repository/maven-mps/")
    }
}

val javadocConfig by configurations.creating {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    // Use JUnit test framework.
    testImplementation("junit:junit:4.13")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.apache.commons:commons-math3:3.6.1")

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation("com.google.guava:guava:29.0-jre")

    javadocConfig("org.eclipse.emf:org.eclipse.emf.ecore:2.28.0")

    // Please note that this forces us to use Java 11 for the javadoc tasks
    // unfortunately earlier version of these libraries, which were compatible with Java 8, are not available
    // on Maven
    javadocConfig("com.jetbrains:mps-openapi:2021.3.1")

    javadocConfig("org.modelix:model-api:1.3.2")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.networknt:json-schema-validator:1.0.77")
}

tasks.register<Javadoc>("generateJavaDocs") {
    source = sourceSets.main.get().allJava
    classpath = javadocConfig
//    options {
//        links("https://docs.oracle.com/javase/8/docs/api/")
//        links("https://download.eclipse.org/modeling/emf/emf/javadoc/2.10.0/")
//        links("https://alexanderpann.github.io/mps-openapi-doc/javadoc_2021.2/")
//    }
}

val isReleaseVersion = !(version as String).endsWith("SNAPSHOT")

tasks.register<Jar>("javadocJar") {
    dependsOn("javadoc")
    from(tasks.getByName("javadoc")/*.destinationDir*/)
    archiveClassifier.set("javadoc")
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    // See https://discuss.gradle.org/t/why-subproject-sourceset-dirs-project-sourceset-dirs/7376/5
    // Without the closure, parent sources are used for children too
    from(sourceSets.getByName("main").java.srcDirs)
}

publishing {

    repositories {
        maven {
            val releaseRepo = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotRepo = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = URI(if (isReleaseVersion) releaseRepo else  snapshotRepo)
            credentials {
                username = if (project.hasProperty("ossrhUsername")) extra["ossrhUsername"] as String else "Unknown user"
                password = if (project.hasProperty("ossrhPassword")) extra["ossrhPassword"] as String else "Unknown password"
            }
        }
    }

    publications {
        create<MavenPublication>("lioncore_java_core") {
            from(components.findByName("java"))
            groupId = "io.lionweb.lioncore-java"
            artifactId = "lioncore-java-" + project.name
            artifact("sourcesJar")
            artifact("javadocJar")
            suppressPomMetadataWarningsFor("cliApiElements")
            suppressPomMetadataWarningsFor("cliRuntimeElements")
            pom {
                name.set("lioncore-java-" + project.name)
                description.set("Java APIs for the LIonWeb system")
                version = project.version as String
                packaging = "jar"
                url.set("https://github.com/LIonWeb-org/lioncore-java")

                scm {
                    connection.set("scm:git:https://github.com/LIonWeb-org/lioncore-java.git")
                    developerConnection.set("scm:git:git@github.com:LIonWeb-org/lioncore-java.git")
                    url.set("https://github.com/LIonWeb-org/lioncore-java.git")
                }

                licenses {
                    license {
                        name.set("Apache Licenve V2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        distribution.set("repo")
                    }
                }

                // The developers entry is strictly required by Maven Central
                developers {
                    developer {
                        id.set("ftomassetti")
                        name.set("Federico Tomassetti")
                        email.set("federico@strumenta.com")
                    }
                    developer {
                        id.set("dslmeinte")
                        name.set("Meinte Boersma")
                        email.set("meinte.boersma@gmail.com")
                    }
                    developer {
                        id.set("enikao")
                        name.set("Niko Stotz")
                        email.set("niko.stotz@nikostotz.de")
                    }
                }

            }
        }
    }
}

val jvmVersion = extra["jvmVersion"] as String

java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
}

if (isReleaseVersion) {
    tasks.withType(Sign::class) {
    }
}
signing {
    sign(publishing.publications["lioncore_java_core"])
}

tasks.register<ConfigureShadowRelocation>("relocateShadowJar") {
    target = tasks.shadowJar.get()
}

tasks.withType<ShadowJar> {
    dependsOn("relocateShadowJar")
    relocate("com.google.gson", "relocated.lionweb.com.google.gson")
}

tasks.named("signLioncore_java_corePublication") {
    dependsOn("shadowJar")
}

val extensiveTestSourceSet = sourceSets.create("extensiveTest") {
    compileClasspath += sourceSets["main"].output
    runtimeClasspath += sourceSets["main"].output
}