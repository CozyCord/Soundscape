import gg.meza.stonecraft.mod
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("gg.meza.stonecraft")
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

modSettings {
    clientOptions {
        fov = 90
        guiScale = 3
        narrator = false
        darkBackground = true
    }
}

repositories {
    maven("https://maven.lavalink.dev/releases")
    mavenCentral()
}

val shadowDeps: Configuration by configurations.creating

dependencies {
    modImplementation("net.fabricmc.fabric-api:fabric-api:${mod.prop("fabric_version")}")

    shadowDeps("dev.arbjerg:lavaplayer:2.2.3") {
        exclude(group = "org.slf4j")
    }
    implementation("dev.arbjerg:lavaplayer:2.2.3") {
        exclude(group = "org.slf4j")
    }

    shadowDeps("dev.lavalink.youtube:v2:1.17.0") {
        exclude(group = "org.slf4j")
    }
    implementation("dev.lavalink.youtube:v2:1.17.0") {
        exclude(group = "org.slf4j")
    }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shadowDeps)
    archiveClassifier.set("dev")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    exclude("META-INF/MANIFEST.MF")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/LICENSE*")
    exclude("META-INF/NOTICE*")
    exclude("META-INF/maven/**")

    relocate("com.sedmelluq", "net.cozystudios.soundscape.shadow.sedmelluq") {
        exclude("com.sedmelluq.discord.lavaplayer.natives.**")
    }
    relocate("dev.arbjerg", "net.cozystudios.soundscape.shadow.arbjerg")
    relocate("org.apache.http", "net.cozystudios.soundscape.shadow.apache.http")
    relocate("org.apache.commons", "net.cozystudios.soundscape.shadow.apache.commons")
    relocate("com.fasterxml", "net.cozystudios.soundscape.shadow.fasterxml")
    relocate("org.jsoup", "net.cozystudios.soundscape.shadow.jsoup")
    relocate("org.mozilla", "net.cozystudios.soundscape.shadow.mozilla")
    relocate("dev.lavalink", "net.cozystudios.soundscape.shadow.lavalink")
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    dependsOn("shadowJar")
    inputFile.set(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
}
