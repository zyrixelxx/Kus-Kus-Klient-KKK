pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://jitpack.io")
        mavenCentral()
    }
}

rootProject.name = "KusKusKlient"
