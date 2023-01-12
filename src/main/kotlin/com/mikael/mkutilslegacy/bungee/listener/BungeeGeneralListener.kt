package com.mikael.mkutilslegacy.bungee.listener

import net.md_5.bungee.api.plugin.Listener

class BungeeGeneralListener : Listener {

    /*
    @EventHandler
    fun onPlayerJoin(e: PostLoginEvent) {
        if (!RedisAPI.isInitialized() || !RedisAPI.useToSyncBungeePlayers) return
        RedisAPI.updateCounter("mkUtils", "mkbungeeapi:playercount", 1)
        // RedisAPI.client!!.set("mkUtils:bungee:players:${e.player.name.lowercase()}", "null")
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerDisconnectEvent) {
        if (!RedisAPI.isInitialized() || !RedisAPI.useToSyncBungeePlayers) return
        RedisAPI.updateCounter("mkUtils", "mkbungeeapi:playercount", -1)
        // RedisAPI.client!!.del("mkUtils:bungee:players:${e.player.name.lowercase()}")
    }

    /*
    @EventHandler
    fun onServerChange(e: ServerConnectedEvent) {
        if (!RedisAPI.isInitialized() || !RedisAPI.useToSyncBungeePlayers) return
        RedisAPI.client!!.set("mkUtils:bungee:players:${e.player.name.lowercase()}", e.server.info.name)
    }
     */
     */
}