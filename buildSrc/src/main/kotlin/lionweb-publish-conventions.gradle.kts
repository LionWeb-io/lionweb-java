// Centralises the Maven Central POM fields that are identical across all published modules.
// Each module must still apply alias(libs.plugins.vt.publish) and call
// mavenPublishing { pom { description.set("...") }; publishToMavenCentral(); signAllPublications() }.

import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

val specsVersion = extra["specsVersion"] as String

// Fires when maven-publish (applied internally by vt.publish) becomes available.
pluginManager.withPlugin("maven-publish") {
    configure<PublishingExtension> {
        publications.withType<MavenPublication>().configureEach {
            groupId = "io.lionweb"
            artifactId = "lionweb-${specsVersion}-${project.name}"
            version = project.version as String
            pom {
                name.set("lionweb-" + project.name)
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
