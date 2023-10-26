import java.net.URI

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

repositories {
    mavenCentral()
}

val javadocConfig by configurations.creating {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    api(project(":core"))

    api("org.eclipse.emf:org.eclipse.emf.common:2.28.0")
    api("org.eclipse.emf:org.eclipse.emf.ecore:2.33.0")
    api("org.eclipse.emf:org.eclipse.emf.ecore.xmi:2.18.0")

    api("org.eclipse.emfcloud:emfjson-jackson:2.2.0")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains:annotations:24.0.0")

    // Use JUnit test framework.
    testImplementation("junit:junit:4.13")
    testImplementation("com.google.code.gson:gson:2.10.1")
}

val jvmVersion = extra["jvmVersion"] as String

java {
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    targetCompatibility = JavaVersion.toVersion(jvmVersion)
}
tasks.register<Javadoc>("myJavadoc") {
    source = sourceSets.main.get().allJava
    classpath = javadocConfig
    options {
        require(this is StandardJavadocDocletOptions)
        addStringOption("link", "https://docs.oracle.com/javase/8/docs/api/")
        addStringOption("link", "https://download.eclipse.org/modeling/emf/emf/javadoc/2.10.0/")
        addStringOption("link", "https://alexanderpann.github.io/mps-openapi-doc/javadoc_2021.2/")
    }
}

val isReleaseVersion = !(version as String).endsWith("SNAPSHOT")

tasks.register<Jar>("javadocJar") {
    dependsOn("myJavadoc")
    from(tasks.getByName("myJavadoc")/*.destinationDir*/)
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
        create<MavenPublication>("lionweb_java_emf") {
            from(components.findByName("java"))
            groupId = "io.lionweb.lionweb-java"
            artifactId = "lionweb-java-" + project.name
            artifact(tasks.findByName("sourcesJar"))
            artifact(tasks.findByName("javadocJar"))
            suppressPomMetadataWarningsFor("cliApiElements")
            suppressPomMetadataWarningsFor("cliRuntimeElements")
            pom {
                name.set("lionweb-java-" + project.name)
                description.set("EMF compatibility layer for LionWeb")
                version = project.version as String
                packaging = "jar"
                url.set("https://github.com/LionWeb-io/lionweb-java")

                scm {
                    connection.set("scm:git:https://github.com/LionWeb-io/lionweb-java.git")
                    developerConnection.set("scm:git:git@github.com:LionWeb-io/lionweb-java.git")
                    url.set("https://github.com/LionWeb-io/lionweb-java.git")
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
                        email.set("github-public@nikostotz.de")
                    }
                }

            }
        }
    }
}

tasks.withType(Sign::class) {
    onlyIf("isReleaseVersion is set") { isReleaseVersion }
}

signing {
    sign(publishing.publications["lionweb_java_emf"])
}