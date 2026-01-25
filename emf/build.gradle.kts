plugins {
    id("java-library")
    alias(libs.plugins.vt.publish)
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

    api(libs.emf.common)
    api(libs.emf.ecore)
    api(libs.emf.ecore.xmi)

    api(libs.emfjson)

    implementation(libs.gson)
    implementation(libs.annotations)

    // Use JUnit test framework.
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.engine)
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
        groupId = "io.lionweb",
        artifactId = "lionweb-${specsVersion}-" + project.name,
        version = project.version as String,
    )

    pom {
        name.set("lionweb-java-" + project.name)
        description.set("EMF compatibility layer for LionWeb")
        version = project.version as String
        packaging = "jar"
        url.set("https://github.com/LionWeb-io/lionweb-jvm")

        scm {
            connection.set("scm:git:https://github.com/LionWeb-io/lionweb-jvm.git")
            developerConnection.set("scm:git:git@github.com:LionWeb-io/lionweb-jvm.git")
            url.set("https://github.com/LionWeb-io/lionweb-jvm.git")
        }

        licenses {
            license {
                name.set("Apache License V2.0")
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
            developer {
                id.set("Ulyana-F1re")
                name.set("Ulyana Tikhonova")
                url.set("https://github.com/Ulyana-F1re")
            }
        }
    }
    publishToMavenCentral(true)
    signAllPublications()
}
