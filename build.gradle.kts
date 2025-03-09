plugins {
    java
}

group = "de.tmxx"
version = "1.0.0"

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        name = "dmulloy2"
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }
}

dependencies {
    // papermc
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")

    // google guice
    compileOnly("com.google.inject:guice:7.0.0")
}

java {
    // setting the language version explicitly to 21 as paper needs that version to run
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.jar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}