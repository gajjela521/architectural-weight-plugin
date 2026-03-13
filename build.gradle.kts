plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.architecture.weight"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // No additional external dependencies needed for this plugin
}

intellij {
    version.set("2023.2.5")
    type.set("IC") // IntelliJ IDEA Community Edition

    plugins.set(listOf("com.intellij.java", "Git4Idea"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
    }
}
