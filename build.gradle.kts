@file:Suppress("warnings")

import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*

plugins {
    java
    kotlin("jvm") version "1.9.24"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("net.minecrell.plugin-yml.bungee") version "0.6.0"
}

group = "com.mikael"
version = "final"

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

val userFolderPath: String = System.getProperty("user.home")

dependencies {
    compileOnly("org.github.paperspigot:paperspigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("io.github.waterfallmc:waterfall-api:1.19-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly(files("libs/spigot-1.8.8.jar"))
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    api("redis.clients:jedis:4.3.1")
    api("org.slf4j:slf4j-api:2.0.5")
    api("org.slf4j:slf4j-log4j12:2.0.5")
    api(files("${userFolderPath}\\Desktop\\IntelliJ Global Depends\\EduardAPI-1.0-all.jar"))
}

fun generateVersion(): String {
    val lastVersion = File("lastversion.txt")
        .apply {
            if (exists()) return@apply
            mkdirs()
            createNewFile()
            writeText("1.0.0-first")
        }
        .readText()
    val newVersion = lastVersion.split("-")[0].split(".")
        .let {
            val nv = it.map { n -> n.toInt() }.toMutableList()
            nv[2]++
            if (nv[2] == 10) {
                nv[2] = 0
                nv[1]++
            }
            if (nv[1] == 10) {
                nv[1] = 0
                nv[0]++
            }
            nv
        }
        .joinToString(".")

    val sha1Bytes = MessageDigest.getInstance("SHA-1").digest(newVersion.toByteArray(StandardCharsets.UTF_8))
    sha1Bytes[6] = (sha1Bytes[6].toInt() and 0x0F or 0x50).toByte() // Define a versão como 5 (UUID baseado em nome)
    sha1Bytes[8] = (sha1Bytes[8].toInt() and 0x3F or 0x80).toByte() // Define a variante como IETF RFC 4122
    val byteBuffer = ByteBuffer.wrap(sha1Bytes)
    val versionVerifier = UUID(byteBuffer.long, byteBuffer.long).toString().split("-")[0]

    val generatedVersion = "${newVersion}-${versionVerifier}"
    File("lastversion.txt").writeText(generatedVersion)

    return generatedVersion
}

val pluginVersion = generateVersion()

bukkit {
    name = "mkUtilsLegacy"
    description = "mkUtils is a lib for plugins development. By Mikael with help of Eduard and Koddy."
    author = "Mikael"
    website = "https://github.com/MikaelMaster/mkUtilsLegacy"

    version = pluginVersion
    softDepend = listOf("Vault")
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "com.mikael.mkutilslegacy.spigot.UtilsMain"
}

bungee {
    name = "mkUtilsLegacy"
    description = "mkUtils is a lib for plugins development. By Mikael with help of Eduard and Koddy."
    author = "Mikael"

    version = pluginVersion
    main = "com.mikael.mkutilslegacy.spigot.UtilsMain"
}

tasks {
    jar {
        destinationDirectory
            .set(file("${userFolderPath}\\Desktop\\Meus Plugins - Jars"))
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
        archiveVersion.set(version.toString())
        archiveBaseName.set("mkUtilsLegacy")
        destinationDirectory.set(file("${userFolderPath}\\Desktop\\Meus Plugins - Jars"))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            //artifacts.add(artifact(tasks.shadowJar))
        }
    }
}