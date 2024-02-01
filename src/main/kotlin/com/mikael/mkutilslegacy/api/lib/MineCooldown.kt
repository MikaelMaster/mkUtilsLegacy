package com.mikael.mkutilslegacy.api.lib

import com.mikael.mkutilslegacy.api.formatDuration
import com.mikael.mkutilslegacy.api.isProxyServer
import com.mikael.mkutilslegacy.api.redis.RedisAPI
import com.mikael.mkutilslegacy.api.toTextComponent
import net.md_5.bungee.api.ProxyServer
import org.bukkit.Bukkit
import java.util.concurrent.TimeUnit

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
 * So, the message "Please wait %times% to use this again." will be transformed into -> "Please wait 3s to use this again." (3 seconds as example)
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

    // Cooldown Properties - Start
    /**
     * The message sent to players during the cooldown.
     *
     * Default: "§cPlease wait §e%time%s §cto use this again.".
     */
    var messageOnCooldown: String? = "§cPlease wait §e%time%s §cto use this again."

    /**
     * A permission to bypass the cooldown.
     * If the player has this permission, the cooldown will be bypassed.
     *
     * Default: *null*. (No bypass permission setted, so no one can bypass the cooldown.)
     */
    var bypassPerm: String? = null

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

    /**
     * The cooldowns by permission.
     * This is a map of permissions and their cooldowns. The key is the permission and the value is the cooldown duration.
     * If the cooldown is set to *null*, this means the permission (player with that permission) will bypass the cooldown.
     * If a player has a permission, the cooldown defined for that permission will be used and [duration] ignored.
     *
     * Example:
     * ```
     * cooldownsByPermission = mapOf(
     *    "mkutils.cooldowns.1" to 20 * 5, // 5 seconds
     *    "mkutils.cooldowns.2" to 20 * 2, // 2 seconds
     *    "mkutils.cooldowns.3" to null // Bypass (no cooldown)
     * )
     *    ````
     *    In this example, players with the permission "mkutils.cooldowns.1" will have a 60 seconds cooldown.
     *    Players with the permission "mkutils.cooldowns.2" will have a 120 seconds cooldown.
     *    Players with the permission "mkutils.cooldowns.3" will bypass the cooldown.
     *    Players without any of these permissions will have the default cooldown defined by [duration].
     *    Players with all on more than one of these permissions will have
     *    the cooldown defined by the lowest cooldown (null is the lowest as possible).
     */
    var cooldownsByPermission = mapOf<String, Long?>()
    // Cooldown Properties - End

    // Cooldown System - Start
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

        var customDuration = duration
        if (cooldownsByPermission.isNotEmpty()) {
            if (isProxyServer) {
                val player = ProxyServer.getInstance().getPlayer(playerName)
                val cooldowns = cooldownsByPermission.filter { player.hasPermission(it.key) }
                if (cooldowns.isNotEmpty()) {
                    customDuration = cooldowns.values.minBy { it ?: 0 } ?: duration
                    if (0 > getResult(playerName)) return true
                }
            } else {
                val player = Bukkit.getPlayer(playerName)
                val cooldowns = cooldownsByPermission.filter { player.hasPermission(it.key) }
                if (cooldowns.isNotEmpty()) {
                    customDuration = cooldowns.values.minBy { it ?: 0 } ?: duration
                    if (0 > getResult(playerName)) return true
                }
            }
        }

        setOnCooldown(playerName, customDuration)
        return true
    }

    fun stopCooldown(playerName: String) {
        if (redisSync) {
            RedisAPI.mapDelete("mkUtils:MineCooldown:Cooldown:${redisSyncId}", playerName)
        } else {
            cooldowns.remove(playerName)
        }
    }

    fun onCooldown(playerName: String): Boolean {
        if (bypassPerm != null) {
            val isProxy = isProxyServer
            return if (isProxyServer) {
                ProxyServer.getInstance().getPlayer(playerName)?.hasPermission(bypassPerm!!) == true
            } else {
                Bukkit.getPlayer(playerName)?.hasPermission(bypassPerm!!) == true
            }
        }
        return getResult(playerName) > 0
    }

    fun setOnCooldown(playerName: String, customDuration: Long): MineCooldown {
        if (onCooldown(playerName)) {
            stopCooldown(playerName)
        }
        if (redisSync) {
            RedisAPI.insertMap(
                "mkUtils:MineCooldown:Cooldown:${redisSyncId}",
                mapOf(
                    playerName to "${System.currentTimeMillis()};${customDuration}"
                )
            )
        } else {
            cooldowns[playerName] = mapOf(System.currentTimeMillis() to customDuration)
        }
        return this
    }

    fun sendOnCooldown(playerName: String) {
        messageOnCooldown?.let {
            if (isProxyServer) {
                ProxyServer.getInstance().getPlayer(playerName)
                    ?.sendMessage(
                        it.replace(
                            "%time%",
                            "${TimeUnit.SECONDS.toMillis(getCooldown(playerName).toLong()).formatDuration()}"
                        ).toTextComponent()
                    )
            } else {
                Bukkit.getPlayer(playerName)
                    ?.sendMessage(
                        it.replace(
                            "%time%",
                            "${TimeUnit.SECONDS.toMillis(getCooldown(playerName).toLong()).formatDuration()}"
                        )
                    )
            }
        }
    }

    fun getResult(playerName: String): Long {
        val timeManager = if (redisSync) {
            val redisData =
                RedisAPI.getMapValue("mkUtils:MineCooldown:Cooldown:${redisSyncId}", playerName)?.split(";") ?: return 0
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
    // Cooldown System - End

}