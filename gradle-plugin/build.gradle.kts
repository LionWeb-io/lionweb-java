import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.2.0"
    alias(libs.plugins.vtpublish)
}

project.group = "io.lionweb"

gradlePlugin {
    website.set("https://github.com/LionWeb-io/LionWeb-java")
    vcsUrl.set("https://github.com/LionWeb-io/lionweb-java.git")
    plugins {
        create("lwGradlePlugin") {
            id = "io.lionweb"
            displayName = "LionWeb Gradle Plugin"
            description = "Generate Java classes for a given LionWeb Language definition"
            tags.set(listOf("lionweb", "language engineering", "MDE"))
            implementationClass = "io.lionweb.gradleplugin.LionWebPlugin"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(libs.gson)
    implementation(libs.protobuf)
    implementation(libs.javapoet)
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// In order to use JavaPoet, we cannot stick to Java 8
java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}

val specsVersion = extra["specsVersion"] as String

//mavenPublishing {
//    coordinates(
//        groupId = "io.lionweb.lionweb-java",
//        artifactId = "lionweb-java-${specsVersion}-" + project.name,
//        version = project.version as String,
//    )
//
//    pom {
//        name.set("lionweb-java-" + project.name)
//        description.set("Java APIs for the LionWeb system")
//        version = project.version as String
//        packaging = "jar"
//        url.set("https://github.com/LionWeb-io/lionweb-java")
//
//        scm {
//            connection.set("scm:git:https://github.com/LionWeb-io/lionweb-java.git")
//            developerConnection.set("scm:git:git@github.com:LionWeb-io/lionweb-java.git")
//            url.set("https://github.com/LionWeb-io/lionweb-java.git")
//        }
//
//        licenses {
//            license {
//                name.set("Apache Licenve V2.0")
//                url.set("https://www.apache.org/licenses/LICENSE-2.0")
//                distribution.set("repo")
//            }
//        }
//
//        // The developers entry is strictly required by Maven Central
//        developers {
//            developer {
//                id.set("ftomassetti")
//                name.set("Federico Tomassetti")
//                email.set("federico@strumenta.com")
//            }
//            developer {
//                id.set("dslmeinte")
//                name.set("Meinte Boersma")
//                email.set("meinte.boersma@gmail.com")
//            }
//            developer {
//                id.set("enikao")
//                name.set("Niko Stotz")
//                email.set("github-public@nikostotz.de")
//            }
//        }
//    }
//    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, true)
//    signAllPublications()
//}