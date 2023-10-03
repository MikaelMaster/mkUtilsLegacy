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

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val player = e.player
        player.runBlock {
            val currentServer = RedisBungeeAPI.spigotServerName
            val servers = mutableMapOf<String, MutableSet<String>>()
            RedisAPI.getMap("mkUtils:BungeeAPI:Servers").forEach {
                servers.getOrPut(it.key) { mutableSetOf() }.addAll(
                    it.value.split(";").filter { l -> l.isNotBlank() }
                )
            }
            servers.values.forEach { set ->
                set.removeIf { it == player.name }
            }
            if (servers.containsKey(currentServer)) {
                servers[currentServer]!!.add(player.name)
            }
            RedisAPI.insertMap("mkUtils:BungeeAPI:Servers",
                servers.mapValues { it.value.joinToString(";") }
            )
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val player = e.player
        player.runBlock {
            val currentServer = RedisBungeeAPI.spigotServerName
            val servers = mutableMapOf<String, MutableSet<String>>()
            RedisAPI.getMap("mkUtils:BungeeAPI:Servers").forEach {
                servers.getOrPut(it.key) { mutableSetOf() }.addAll(
                    it.value.split(";").filter { l -> l.isNotBlank() }
                )
            }
            servers[currentServer]?.let { serverPlayers ->
                serverPlayers.removeIf { it == player.name }
            }

            RedisAPI.insertMap("mkUtils:BungeeAPI:Servers",
                servers.mapValues { it.value.joinToString(";") }
            )
        }
    }

}