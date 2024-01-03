import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-library")
    id("com.vanniktech.maven.publish")
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
    api(project(":emf-builtins"))

    api(emf.common)
    api(emf.ecore)
    api(emf.ecore.xmi)

    api(emf.emfjson)

    implementation(libs.gson)
    implementation(libs.annotations)

    // Use JUnit test framework.
    testImplementation(libs.junit)
    testImplementation(libs.gson)
}

val jvmVersion = extra["jvmVersion"] as String
val specsVersion = extra["specsVersion"] as String

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

mavenPublishing {
    coordinates(
        groupId = "io.lionweb.lionweb-java",
        artifactId = "lionweb-java-${specsVersion}-" + project.name,
        version = project.version as String,
    )

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
    publishToMavenCentral(SonatypeHost.S01, true)
    signAllPublications()
}
