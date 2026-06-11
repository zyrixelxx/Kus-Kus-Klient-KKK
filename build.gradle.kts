plugins {
    id("fabric-loom") version "1.17.0-alpha.13"
    id("maven-publish")
}

version = property("mod_version") as String
group = property("maven_group") as String

base {
    archivesName.set(property("archives_base_name") as String)
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    implementation("com.github.jagrosh:DiscordIPC:a8d6631cc90b25f1ede2178b99ad19d016f002a0")
    include("com.github.jagrosh:DiscordIPC:a8d6631cc90b25f1ede2178b99ad19d016f002a0")
    include("org.json:json:20230227")
    include("com.kohlschutter.junixsocket:junixsocket-common:2.6.2")
    include("com.kohlschutter.junixsocket:junixsocket-native-common:2.6.2")
}

loom {
    splitEnvironmentSourceSets()
    mods {
        create("kuskusklient") {
            sourceSet(sourceSets["client"])
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
