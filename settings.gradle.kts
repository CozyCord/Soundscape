pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net/")
    }
}

plugins {
    id("gg.meza.stonecraft") version "1.9.+"
    id("dev.kikugie.stonecutter") version "0.8.+"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    shared {
        fun mc(version: String) {
            version("$version-fabric", version)
        }
        mc("1.20.1")
        mc("1.21.1")
        mc("1.21.4")
        mc("1.21.11")

        vcsVersion = "1.21.1-fabric"
    }
    create(rootProject)
}

rootProject.name = "Soundscape"
