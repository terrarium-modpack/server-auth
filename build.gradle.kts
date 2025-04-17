plugins {
    kotlin("jvm") version "2.1.20"
    id("dev.architectury.loom") version "1.10-SNAPSHOT"
}

version = "1.1.0"
group = "dev.optimistic"

repositories {
    maven("https://maven.parchmentmc.org")
    maven("https://maven.neoforged.net/releases")
    maven("https://thedarkcolour.github.io/KotlinForForge")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21.1:2024.11.17@zip")
    })

    neoForge("net.neoforged:neoforge:21.1.148")
    implementation("thedarkcolour:kotlinforforge:5.7.0")
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

tasks {
    jar {
        from("LICENSE") { rename { "${project.name}_${it}" } }
    }

    withType<JavaCompile>().configureEach {
        options.release = 21
        options.encoding = "UTF-8"
    }

    processResources {
        filesMatching("META-INF/neoforge.mods.toml" ) {
            expand("version" to project.version)
        }
    }
}
