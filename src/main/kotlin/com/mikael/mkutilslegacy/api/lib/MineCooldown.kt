package com.mikael.mkutilslegacy.api.lib

import com.mikael.mkutilslegacy.api.isProxyServer
import com.mikael.mkutilslegacy.api.toTextComponent
import net.md_5.bungee.api.ProxyServer
import org.bukkit.Bukkit

/**
 * [MineCooldown] util class (can be used on ProxyServer and BukkitServer)
 *
 * This class represents a 'Delay Manager', 'Cooldown Manager' or just a 'Simple Cooldown'.
 *
 * To create/invoke a new MineItem you can use:
 * - val clw = MineCooldown(ticks: [Long]) -> 20 ticks = 1s. So, with '20' as parameter, you'll create an 1-second delay.
 *
 * Then, you can use clw.apply { *code* } to set your custom [messageOnCooldown] for example.
 * Important: Inside [messageOnCooldown] use the placeholder '%time' to get the seconds.
 * So, the message "Please wait %times to use this again." will be transformed into -> "Please wait 3s to use this again." (3 seconds as example)
 *
 * To use, just do as the example bellow:
 * - clw.cooldown(playerName: [String]) { *code to be executed under delay* }
 *
 * If you ask for the function again, and the delay is still runnning, the player will receive the [messageOnCooldown] automatically.
 *
 * @param duration the cooldown duration (in ticks) to create a new MineCooldown. 20 ticks = 1s.
 * @author Mikael
 */
class MineCooldown(var duration: Long) {

    var messageOnCooldown: String? = "§cPlease wait §e%times §cto use this again."

    fun noMessages() {
        messageOnCooldown = null
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
        cooldowns.remove(playerName)
    }

    fun onCooldown(playerName: String): Boolean {
        return getResult(playerName) > 0
    }

    fun setOnCooldown(playerName: String): MineCooldown {
        if (onCooldown(playerName)) {
            stopCooldown(playerName)
        }
        cooldowns[playerName] = mapOf(System.currentTimeMillis() to duration)
        return this
    }

    fun sendOnCooldown(playerName: String) {
        messageOnCooldown?.let {
            if (isProxyServer) {
                ProxyServer.getInstance().getPlayer(playerName)
                    ?.sendMessage(it.replace("%time", "${getCooldown(playerName)}").toTextComponent())
            } else {
                Bukkit.getOnlinePlayers().firstOrNull { player -> player.name == playerName }
                    ?.sendMessage(it.replace("%time", "${getCooldown(playerName)}"))
            }
        }
    }

    fun getResult(playerName: String): Long {
        if (cooldowns.containsKey(playerName)) {
            val now = System.currentTimeMillis()
            val timeManager = cooldowns[playerName]!!
            val before = timeManager.keys.first()
            val cooldownDuration = timeManager.values.first() * 50
            val endOfCooldown = before + cooldownDuration
            val durationLeft = endOfCooldown - now
            return if (durationLeft <= 0) {
                0
            } else durationLeft / 50
        }
        return 0
    }

    fun getCooldown(playerName: String): Int {
        return (getResult(playerName) / 20).toInt() + 1
    }
}