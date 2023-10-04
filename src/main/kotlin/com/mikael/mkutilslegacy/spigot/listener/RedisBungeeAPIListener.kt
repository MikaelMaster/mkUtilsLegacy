package com.mikael.mkutilslegacy.spigot.listener

import com.mikael.mkutilslegacy.api.redis.RedisAPI
import com.mikael.mkutilslegacy.api.redis.RedisBungeeAPI
import com.mikael.mkutilslegacy.spigot.api.lib.MineListener
import com.mikael.mkutilslegacy.spigot.api.runBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class RedisBungeeAPIListener : MineListener() {

    private val currentServer = RedisBungeeAPI.spigotServerName

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val player = e.player
        player.runBlock {
            val serverData = RedisAPI.getMapValue("mkUtils:BungeeAPI:Servers", currentServer) ?: return@runBlock
            val players = serverData.split(";").filter { it.isNotBlank() }.toMutableList()
            players.add(player.name)
            RedisAPI.insertMap("mkUtils:BungeeAPI:Servers", mapOf(currentServer to players.joinToString(";")))
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val player = e.player
        player.runBlock {
            val serverData = RedisAPI.getMapValue("mkUtils:BungeeAPI:Servers", currentServer) ?: return@runBlock
            val players = serverData.split(";").filter { it.isNotBlank() }.toMutableList()
            players.remove(player.name)
            RedisAPI.insertMap("mkUtils:BungeeAPI:Servers", mapOf(currentServer to players.joinToString(";")))
        }
    }

}