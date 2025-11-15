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