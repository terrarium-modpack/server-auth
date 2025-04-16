plugins {
    kotlin("jvm") version "2.1.20"
    id("fabric-loom") version "1.10-SNAPSHOT"
}

version = "1.0.5-SNAPSHOT"
group = "dev.optimistic"

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.14")

    setOf("fabric-api-base", "fabric-command-api-v2").forEach {
        modImplementation(fabricApi.module(it, "0.92.5+1.20.1"))
    }

    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.2+kotlin.2.1.20")
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

tasks {
    jar {
        from("LICENSE") { rename { "${project.name}_${it}" } }
    }

    withType<JavaCompile>().configureEach {
        options.release = 17
        options.encoding = "UTF-8"
    }

    processResources {
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
}
