plugins {
    kotlin("jvm") version "2.1.20"
    id("fabric-loom") version "1.10-SNAPSHOT"
}

version = "1.0.5-SNAPSHOT"
group = "dev.optimistic"

repositories {
    maven("https://maven.parchmentmc.org")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21.1:2024.11.17@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:0.16.14")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.2+kotlin.2.1.20")
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
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
}
