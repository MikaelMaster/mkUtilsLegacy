package com.mikael.mkutilslegacy.bungee.listener

import com.mikael.mkutilslegacy.api.redis.RedisAPI
import com.mikael.mkutilslegacy.api.redis.RedisBungeeAPI
import com.mikael.mkutilslegacy.bungee.api.lib.ProxyListener
import com.mikael.mkutilslegacy.bungee.api.runBlock
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

/**
 * Listener for [RedisBungeeAPI].
 */
class RedisBungeeAPIListener : ProxyListener() {

    @EventHandler(priority = EventPriority.LOWEST) // Called before everything
    fun onServerChange(e: ServerConnectedEvent) {
        val player = e.player
        player.runBlock {
            if (!RedisBungeeAPI.isEnabled) return@runBlock
            val newServer = e.server?.info?.name ?: return@runBlock
            val servers = mutableMapOf<String, MutableSet<String>>()
            RedisAPI.getMap("mkUtils:BungeeAPI:Servers").forEach {
                servers.getOrPut(it.key) { mutableSetOf() }.addAll(
                    it.value.split(";").filter { l -> l.isNotBlank() }
                )
            }
            servers.values.forEach { set ->
                set.removeIf { it == player.name }
            }
            if (servers.containsKey(newServer) && player.isConnected) {
                servers[newServer]!!.add(player.name)
            }
            RedisAPI.insertMap("mkUtils:BungeeAPI:Servers",
                servers.mapValues { it.value.joinToString(";") }
            )
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // Called after everything
    fun onPlayerQuit(e: PlayerDisconnectEvent) {
        val player = e.player
        player.runBlock {
            if (!RedisBungeeAPI.isEnabled) return@runBlock
            val servers = mutableMapOf<String, MutableSet<String>>()
            RedisAPI.getMap("mkUtils:BungeeAPI:Servers").forEach {
                servers.getOrPut(it.key) { mutableSetOf() }.addAll(
                    it.value.split(";").filter { l -> l.isNotBlank() }
                )
            }
            servers.values.forEach { set ->
                set.removeIf { it == player.name }
            }
            RedisAPI.insertMap("mkUtils:BungeeAPI:Servers",
                servers.mapValues { it.value.joinToString(";") }
            )
        }
    }

}