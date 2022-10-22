plugins {
    java
    kotlin("jvm") version "1.7.20"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.mikael"
version = "1.0"

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io/")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    compileOnly("org.github.paperspigot:paperspigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.16-R0.3")
    compileOnly(files("libs/spigot-1.8.8.jar"))
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    api("redis.clients:jedis:4.3.0")
    api("org.slf4j:slf4j-api:2.0.3")
    api("org.slf4j:slf4j-log4j12:2.0.3")
    api(files("C:\\Users\\Koddy\\Desktop\\Jars\\EduardAPI-1.0-all.jar"))
}

tasks {
    jar {
        destinationDirectory
            .set(file("C:\\Users\\Koddy\\Desktop\\Jars\\Plugins"))
    }
    compileJava {
        options.encoding = "UTF-8"
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    shadowJar {
        archiveVersion.set("1.0")
        archiveBaseName.set("mkUtilsLegacy")
        destinationDirectory.set(
            file("C:\\Users\\Koddy\\Desktop\\Jars\\Plugins")
        )
    }
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}