plugins {
    kotlin("jvm") version "1.9.22"
    java
    id("io.github.goooler.shadow") version "8.1.8"
}

group = "cubeplex"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }

    shadowJar {
        archiveBaseName.set("CubeplexBackpack")
        archiveClassifier.set("")
        relocate("kotlin", "cubeplex.libs.kotlin")
    }

    build {
        dependsOn(shadowJar)
    }
}
