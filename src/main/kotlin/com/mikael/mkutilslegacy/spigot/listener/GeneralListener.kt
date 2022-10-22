package com.mikael.mkutilslegacy.spigot.listener

import com.mikael.mkutilslegacy.api.Redis
import com.mikael.mkutilslegacy.api.redis.RedisAPI
import com.mikael.mkutilslegacy.api.redis.RedisBungeeAPI
import com.mikael.mkutilslegacy.spigot.UtilsMain
import com.mikael.mkutilslegacy.spigot.api.lib.MineListener
import com.mikael.mkutilslegacy.spigot.api.runBlock
import net.eduard.api.lib.modules.Mine
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class GeneralListener : MineListener() {
    companion object {
        lateinit var instance: GeneralListener
    }

    init {
        instance = this@GeneralListener

        UtilsMain.instance.syncTimer(20 * 3, 20 * 3) { // Invincible Entity System timer
            invincibleEntities.removeIf { it.isDead }
        }
    }

    /**
     * Invincible Entity System.
     */

    val invincibleEntities = mutableSetOf<Entity>()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDamage(e: EntityDamageEvent) {
        if (!invincibleEntities.contains(e.entity)) return
        e.isCancelled = true
    }

    /**
     * [RedisBungeeAPI] extra - listeners.
     */

    @EventHandler(priority = EventPriority.MONITOR) // Should be MONITOR
    fun onJoin(e: PlayerJoinEvent) {
        if (!RedisAPI.useToSyncBungeePlayers) return
        val player = e.player
        player.runBlock {
            val newPlayersList = mutableListOf<String>()
            Mine.getPlayers().forEach { newPlayersList.add(it.name) }
            Redis.insertStringList("mkUtils", "BungeeAPI:Servers:${RedisBungeeAPI.spigotServerName}:Players", newPlayersList, false)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR) // Should be MONITOR
    fun onQuit(e: PlayerQuitEvent) {
        if (!RedisAPI.useToSyncBungeePlayers) return
        val player = e.player
        player.runBlock {
            val newPlayersList = mutableListOf<String>()
            Mine.getPlayers().forEach { newPlayersList.add(it.name) }
            newPlayersList.removeIf { it == player.name }
            Redis.insertStringList("mkUtils", "BungeeAPI:Servers:${RedisBungeeAPI.spigotServerName}:Players", newPlayersList, false)
        }
    }

}