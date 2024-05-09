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

    private val currentServer = RedisBungeeAPI.Spigot.spigotServerName

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val player = e.player
        player.runBlock {
            RedisAPI.jedisPool.resource.use { resource ->
                val pipeline = resource.pipelined()
                pipeline.sadd(
                    "${RedisBungeeAPI.RedisBungeeAPIKey.SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${currentServer}",
                    player.name.lowercase()
                )
                pipeline.sync()
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val player = e.player
        player.runBlock {
            RedisAPI.jedisPool.resource.use { resource ->
                val pipeline = resource.pipelined()
                pipeline.srem(
                    "${RedisBungeeAPI.RedisBungeeAPIKey.SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${currentServer}",
                    player.name.lowercase()
                )
                pipeline.sync()
            }
        }
    }

}