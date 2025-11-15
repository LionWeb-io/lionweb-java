plugins {
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("lwGradlePlugin") {
            id = "io.lionweb.gradleplugin"
            implementationClass = "io.lionweb.gradleplugin.LionWebPlugin"
        }
    }
}

dependencies {
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}