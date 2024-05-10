package com.mikael.mkutilslegacy.bungee.listener

import com.mikael.mkutilslegacy.api.redis.RedisAPI
import com.mikael.mkutilslegacy.api.redis.RedisBungeeAPI
import com.mikael.mkutilslegacy.bungee.api.lib.ProxyListener
import com.mikael.mkutilslegacy.bungee.api.runBlock
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class RedisBungeeAPIListener : ProxyListener() {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDisconnect(e: PlayerDisconnectEvent) {
        val player = e.player
        player.runBlock {
            RedisAPI.jedisPool.resource.use { resource ->
                val pipeline = resource.pipelined()
                pipeline.hdel(RedisBungeeAPI.RedisBungeeAPIKey.ONLINE_PLAYERS_SERVERS.key, player.name.lowercase())
                pipeline.sync()
            }
        }
    }

}