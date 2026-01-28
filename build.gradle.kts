import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    alias(libs.plugins.release)
    alias(libs.plugins.vt.publish) apply (false)
    alias(libs.plugins.kotlin.jvm) apply (false)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.dokka) apply (false)
    alias(libs.plugins.versioncheck)
    alias(libs.plugins.spotless)
    id("java")
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    spotless {
        java {
            googleJavaFormat()
            targetExclude("**/src-gen/**", "**/build/**")
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

allprojects {

    group = "io.lionweb.lionweb-kotlin"
    project.version = version

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

release {
    buildTasks =
        listOf(
            ":core:publishAllPublicationsToMavenCentralRepository",
            ":extensions:publishAllPublicationsToMavenCentralRepository",
            ":client:publishAllPublicationsToMavenCentralRepository",
            ":client-testing:publishAllPublicationsToMavenCentralRepository",
            ":emf:publishAllPublicationsToMavenCentralRepository",
            ":emf-builtins:publishAllPublicationsToMavenCentralRepository",
            ":gradle-plugin:publishPlugins",
            ":core:publishAllPublicationsToMavenCentralRepository",
            ":client:publishAllPublicationsToMavenCentralRepository",
        )
    versionPropertyFile = "./gradle.properties"
    git {
        requireBranch.set("main")
        pushToRemote.set("origin")
    }
}

tasks.wrapper {
    gradleVersion = "8.14.3"
    distributionType = Wrapper.DistributionType.ALL
}

gradle.projectsEvaluated {
    tasks.register<Javadoc>("aggregateJavadoc", Javadoc::class) {
        group = "documentation"
        description = "Aggregate Javadoc from all subprojects"
        destinationDir = file("$rootDir/website/static/api")

        val includedProjects = subprojects.filter { it.name != "docs-examples" }

        val allSources =
            files(
                includedProjects.mapNotNull {
                    it.extensions
                        .findByType<JavaPluginExtension>()
                        ?.sourceSets
                        ?.findByName("main")
                        ?.allJava
                },
            ).asFileTree

        val allClasspaths =
            files(
                includedProjects.mapNotNull {
                    it.extensions
                        .findByType<JavaPluginExtension>()
                        ?.sourceSets
                        ?.findByName("main")
                        ?.compileClasspath
                },
            )

        source = allSources
        classpath = allClasspaths

        val javadocOptions = options as StandardJavadocDocletOptions
        javadocOptions.encoding = "UTF-8"
        javadocOptions.charSet = "UTF-8"
        javadocOptions.setAuthor(true)
        javadocOptions.setVersion(true)
        javadocOptions.links("https://docs.oracle.com/javase/8/docs/api/")
        javadocOptions.addStringOption("Xdoclint:none", "-quiet")

        doFirst {
            println("Javadoc will be generated from:")
            allSources.forEach { println("  - $it") }
        }
    }
}

tasks.register("integrationTest") {
    group = "verification"
    description = "Runs core module integration tests"
    dependsOn(":core:integrationTest")
}
