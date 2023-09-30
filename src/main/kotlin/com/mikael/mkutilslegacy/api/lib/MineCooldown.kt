package com.mikael.mkutilslegacy.api.lib

import com.mikael.mkutilslegacy.api.isProxyServer
import com.mikael.mkutilslegacy.api.redis.RedisAPI
import com.mikael.mkutilslegacy.api.toTextComponent
import net.md_5.bungee.api.ProxyServer
import org.bukkit.Bukkit

/**
 * [MineCooldown] util class (Hybrid - can be used on ProxyServer and BukkitServer)
 *
 * This class represents a 'Delay Manager', 'Cooldown Manager' or just a 'Simple Cooldown'.
 *
 * To create/invoke a new MineCooldown you can use:
 * - val clw = MineCooldown(ticks: [Long]) -> 20 ticks = 1s. *So, with '20' as parameter, you'll create an 1-second delay.*
 *
 * Then, you can use clw.apply { *code* } to set your custom [messageOnCooldown] for example.
 * Important: Inside [messageOnCooldown] use the placeholder '%time%' to get the seconds remaining.
 * So, the message "Please wait %times%s to use this again." will be transformed into -> "Please wait 3s to use this again." (3 seconds as example)
 *
 * To use, just do as the example bellow:
 * - clw.cooldown(playerName: [String]) { *code to be executed under delay* }
 *
 * If you ask for the function again, and the delay is still runnning, the player will receive the [messageOnCooldown] automatically.
 * Remember that if [messageOnCooldown] is null ([noMessages]) the player will receive nothing.
 *
 * @param duration the cooldown duration (in ticks) to create a new MineCooldown. 20 ticks = 1s.
 * @author Mikael
 */
@Suppress("WARNINGS")
class MineCooldown(var duration: Long) {

    /**
     * The message sent to players during the cooldown.
     *
     * Default: "§cPlease wait §e%time%s §cto use this again.".
     */
    var messageOnCooldown: String? = "§cPlease wait §e%time%s §cto use this again."

    /**
     * Sets the [messageOnCooldown] to null.
     * Then, No message will be sent to players during cooldown.
     *
     * This is usefully to very small delays.
     * For example: Delay 5-tick a ClickEvent.
     */
    fun noMessages() {
        messageOnCooldown = null
    }

    /**
     * Defines if this [MineCooldown] should be synced using the [RedisAPI].
     * If this is set to true, [cooldowns] will be saved and synced using redis.
     *
     * @see redisSyncId
     */
    var redisSync = false

    /**
     * The ID of this [MineCooldown] under redis.
     * If you want that different cooldowns in different servers sync between, just set them with the same ID.
     *
     * By default, the id is represented as [MineCooldown].toString(), using MineCooldown as this.
     *
     * @see redisSync
     */
    var redisSyncId = this.toString()

    init {
        if (redisSync && !RedisAPI.isInitialized()) {
            error("Cannot use redis to sync cooldown data; RedisAPI is not initialized.")
        }
    }

    @Transient
    val cooldowns = mutableMapOf<String, Map<Long, Long>>()

    fun cooldown(playerName: String): Boolean {
        if (onCooldown(playerName)) {
            sendOnCooldown(playerName)
            return false
        }
        setOnCooldown(playerName)
        return true
    }

    fun stopCooldown(playerName: String) {
        if (redisSync) {
            RedisAPI.delete("mkUtils:MineCooldown:Cooldown:${redisSyncId}:${playerName}")
        } else {
            cooldowns.remove(playerName)
        }
    }

    fun onCooldown(playerName: String): Boolean {
        return getResult(playerName) > 0
    }

    fun setOnCooldown(playerName: String): MineCooldown {
        if (onCooldown(playerName)) {
            stopCooldown(playerName)
        }
        if (redisSync) {
            RedisAPI.insert(
                "mkUtils:MineCooldown:Cooldown:${redisSyncId}:${playerName}",
                "${System.currentTimeMillis()};${duration}"
            )
        } else {
            cooldowns[playerName] = mapOf(System.currentTimeMillis() to duration)
        }
        return this
    }

    fun sendOnCooldown(playerName: String) {
        messageOnCooldown?.let {
            if (isProxyServer) {
                ProxyServer.getInstance().getPlayer(playerName)
                    ?.sendMessage(*it.replace("%time%", "${getCooldown(playerName)}").toTextComponent())
            } else {
                Bukkit.getOnlinePlayers().firstOrNull { player -> player.name == playerName }
                    ?.sendMessage(it.replace("%time%", "${getCooldown(playerName)}"))
            }
        }
    }

    fun getResult(playerName: String): Long {
        val timeManager = if (redisSync) {
            val redisData =
                RedisAPI.getString("mkUtils:MineCooldown:Cooldown:${redisSyncId}:${playerName}")?.split(";") ?: return 0
            mapOf(redisData[0].toLong() to redisData[1].toLong())
        } else {
            cooldowns[playerName] ?: return 0
        }
        val now = System.currentTimeMillis()
        val before = timeManager.keys.first()
        val cooldownDuration = timeManager.values.first() * 50
        val endOfCooldown = before + cooldownDuration
        val durationLeft = endOfCooldown - now
        return if (durationLeft <= 0) 0 else durationLeft / 50
    }

    fun getCooldown(playerName: String): Int {
        return (getResult(playerName) / 20).toInt() + 1
    }
}