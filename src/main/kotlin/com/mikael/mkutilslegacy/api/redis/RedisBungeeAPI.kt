package com.mikael.mkutilslegacy.api.redis

import com.mikael.mkutilslegacy.api.Redis
import com.mikael.mkutilslegacy.api.isProxyServer
import com.mikael.mkutilslegacy.api.redis.RedisAPI.client
import com.mikael.mkutilslegacy.api.redis.RedisAPI.clientConnection
import com.mikael.mkutilslegacy.api.syncMysqUpdatesKey
import com.mikael.mkutilslegacy.api.toTextComponent
import com.mikael.mkutilslegacy.bungee.UtilsBungeeMain
import com.mikael.mkutilslegacy.spigot.UtilsMain
import net.eduard.api.lib.modules.Extra
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.scheduler.ScheduledTask
import org.bukkit.scheduler.BukkitTask
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub

object RedisBungeeAPI {

    /**
     * Should NOT be used on Proxy Server side.
     *
     * @return the name of this spigot server set on [UtilsMain.config] file. (RedisBungeeAPI.spigotServerName: "lobby-1")
     * @throws NullPointerException if used on Proxy Server.
     */
    val spigotServerName: String get() = UtilsMain.instance.config.getString("RedisBungeeAPI.spigotServerName")

    /**
     * @return a new connected Redis Client ([Jedis]).
     * @see RedisAPI.connectExtraClient
     */
    private fun getExtraJedis(): Jedis {
        synchronized(syncMysqUpdatesKey) {
            val connectionData =
                if (isProxyServer) UtilsBungeeMain.instance.config["Redis", RedisConnectionData::class.java]
                else UtilsMain.instance.config["Redis", RedisConnectionData::class.java]
            val extraJedis = Redis.createExtraClient(connectionData)
            Redis.connectExtraClient(extraJedis, connectionData)
            return extraJedis
        }
    }

    /**
     * Please note that *servers will be returned as it's on the mkUtils Spigot Server Config File*.
     *
     * @return a list with all Spigot Servers registered on mkUtils RedisBungeeAPI on this same Redis Server.
     * @throws IllegalStateException if the [RedisAPI] [client] or the [clientConnection] is null.
     * @see RedisAPI.getStringList
     */
    fun getSpigotServers(): List<String> {
        return Redis.getStringList("mkUtils", "BungeeAPI:Servers")
    }

    /**
     * It'll return the given [playerName] current Spigot Server name.
     *
     * @param playerName the player to get his current connected Spigot Server name.
     * @return the given [playerName] Spigot Server name. Can be null if the given [playerName] server is null.
     * @throws IllegalStateException if the [RedisAPI] [client] or the [clientConnection] is null.
     * @see getSpigotServers
     * @see getOnlinePlayers(serverName)
     */
    fun getPlayerServer(playerName: String): String? {
        var toReturn: String? = null
        for (server in getSpigotServers()) {
            val possibleServer = getOnlinePlayers(server).firstOrNull { it.equals(playerName, true) }
            if (possibleServer != null) {
                toReturn = server; break
            } else continue
        }
        return toReturn
    }

    /**
     * It'll return all online players names logged in on all registered Spigot Severs.
     *
     * @return a list with all online players names. The list may be empty if there's no online player.
     * @throws IllegalStateException if the [RedisAPI] [client] or the [clientConnection] is null.
     * @see getSpigotServers
     * @see getOnlinePlayers(serverName)
     */
    fun getOnlinePlayers(): List<String> {
        val players = mutableListOf<String>()
        for (server in getSpigotServers()) {
            players.addAll(getOnlinePlayers(server))
        }
        return players
    }

    /**
     * It'll return all online players names on logged in on the given [serverName].
     *
     * @param serverName the Spigot Server to get online players names list.
     * @return a list with all online players names on the given Spigot Server. The list may be empty if the given server doesn't exist.
     * @throws IllegalStateException if the [RedisAPI] [client] or the [clientConnection] is null.
     * @see RedisAPI.getStringList
     */
    fun getOnlinePlayers(serverName: String): List<String> {
        if (!Redis.client!!.exists("mkUtils:BungeeAPI:Servers:$serverName:Players")) return emptyList()
        return Redis.getStringList("mkUtils", "BungeeAPI:Servers:$serverName:Players")
    }

    /**
     * Use to connect a player to a  specific Spigot Server.
     * It'll send a message to Proxy(s) to connect the given [playerName] to the given [serverName].
     * If the proxy that receive this message don't have the given player online, nothing will happen.
     *
     * @param playerName the player to connect.
     * @param serverName the server to connect the given [playerName].
     * @return True if the request has been sent with success. Otherwise, false.
     * @throws IllegalStateException if the [RedisAPI] [client] or the [clientConnection] is null.
     */
    fun connectToServer(playerName: String, serverName: String): Boolean {
        return Redis.sendEvent("mkUtils:BungeeAPI:Event:ConnectPlayer", "${playerName};${serverName}")
    }

    fun kickPlayer(playerName: String, kickReason: String): Boolean {
        if (kickReason.contains(";")) error("kickReason cannot contains ';' because of internal separator")
        return Redis.sendEvent("mkUtils:BungeeAPI:Event:KickPlayer", "${playerName};${kickReason}")
    }

    /**
     * It'll send a text message (on chat) to the given [playerName], through Redis. The player will receive this message, regardless of the Proxy it is connected to.
     *
     * @param playerName the player that will receive the given [message].
     * @param message the message to send to the given [playerName].
     * @return True if the request has been sent with success. Otherwise, false.
     * @throws IllegalStateException if the given [message] contains the character ';'.
     * @throws IllegalStateException if the [RedisAPI] [client] or the [clientConnection] is null.
     * @see RedisAPI.sendEvent
     */
    fun sendMessage(playerName: String, message: String): Boolean {
        if (message.contains(";")) error("message cannot contains ';' because of internal separator")
        return Redis.sendEvent("mkUtils:BungeeAPI:Event:SendMsgToPlayer", "${playerName};${message}")
    }

    /**
     * It'll return the online player amount of the given [serverName].
     *
     * @param serverName the Spigot Server to get online player amount.
     * @return The player amount (Int). It'll return -1 if the given [serverName] is not online or does not exist.
     * @throws IllegalStateException if the [RedisAPI] [client] or the [clientConnection] is null.
     * @see getOnlinePlayers(serverName)
     */
    fun getPlayerAmount(serverName: String): Int {
        if (!Redis.client!!.exists("mkUtils:BungeeAPI:Servers:$serverName:Players")) return -1
        return getOnlinePlayers(serverName).size
    }

    /**
     * It'll return the global player amount (all connected players amount/size).
     *
     * @return The global player amount (Int).
     * @throws IllegalStateException if the [RedisAPI] [client] or the [clientConnection] is null.
     * @see getOnlinePlayers
     */
    fun getPlayerAmount(): Int {
        return getOnlinePlayers().size
    }

    /**
     * Kicks a specific player. The given [playerName] will be disconnected from his current Proxy Server.
     *
     * @param playerName the player name to kick.
     * @param kickReason the message to kick this player. Cannot contains ';'.
     * @param bypassPerm the bypass permission. If the player have this permission, he'll not be kicked.
     * @return True if the request has been sent with success. Otherwise, false.
     * @throws IllegalStateException if the given [kickReason] contains ';'.
     * @throws IllegalStateException if the [RedisAPI] [client] or the [clientConnection] is null.
     * @see RedisAPI.sendEvent
     */
    fun kickPlayer(playerName: String, kickReason: String, bypassPerm: String? = "none"): Boolean {
        if (kickReason.contains(";")) error("kickReason cannot contains ';' because of internal separator")
        return Redis.sendEvent("mkUtils:BungeeAPI:Event:KickPlayer", "${playerName};${kickReason}")
    }

    /**
     * Get player amount of all Proxy(ies). (Global player amount)
     *
     * @return The global player amount (Int).
     * @see getGlobalPlayerAmount
     */
    fun getGlobalPlayerAmount(): Int {
        return getOnlinePlayers().size
    }

    // EXTRA SECTION

    var bukkitServerTask: BukkitTask? = null

    fun bukkitServerOnEnable() {
        bukkitServerTask = UtilsMain.instance.asyncTask {
            /*
            val connectionData = UtilsMain.instance.config["Redis", RedisConnectionData::class.java]
            val extraJedis = RedisAPI.createExtraClient(connectionData)
            RedisAPI.connectExtraClient(extraJedis, connectionData)

            extraJedis.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String, message: String) {
                    if (channel != "mkUtils:RedisBungeeAPI:ServerData:Players") return
                    val data = message.split(";")
                    val server = data[0]
                    val playersString = data[1]
                    if (playersString.isEmpty()) {
                        playersCache[server.lowercase()] = mutableListOf()
                        return
                    }
                    val players = playersString.split(",")
                    synchronized(syncMysqUpdatesKey) {
                        playersCache[server.lowercase()] = players.toMutableList()
                    }
                }
            }, "mkUtils:RedisBungeeAPI:ServerData:Players")

             */
        }
    }

    var proxyServerTask: ScheduledTask? = null

    fun proxyServerOnEnable() {

        ProxyServer.getInstance().scheduler.runAsync(UtilsBungeeMain.instance) {
            getExtraJedis().subscribe(
                object : JedisPubSub() {
                    override fun onMessage(channel: String, message: String) {
                        if (channel == "mkUtils:BungeeAPI:Event:ConnectPlayer") {
                            val data = message.split(";")
                            val player = ProxyServer.getInstance().getPlayer(data[0]) ?: return
                            val server = ProxyServer.getInstance().getServerInfo(data[1]) ?: return
                            player.connect(server)
                        }
                        if (channel == "mkUtils:BungeeAPI:Event:KickPlayer") {
                            val data = message.split(";")
                            val player = ProxyServer.getInstance().getPlayer(data[0]) ?: return
                            player.disconnect(Extra.getText(1, *data.toTypedArray()).toTextComponent())
                        }
                        if (channel == "mkUtils:BungeeAPI:Event:SendMsgToPlayer") {
                            val data = message.split(";")
                            val player = ProxyServer.getInstance().getPlayer(data[0]) ?: return
                            player.sendMessage(Extra.getText(1, *data.toTypedArray()).toTextComponent())
                        }
                        if (channel == "mkUtils:BungeeAPI:Event:ServerPowerAction") {
                            if (!UtilsBungeeMain.instance.config.getBoolean("RedisBungeeAPI.logSpigotServersPowerActions")) return
                            val data = message.split(";")
                            val server = data[0]
                            val newState = data[1]
                            val logMsg = if (newState == "on") "§aThe spigot server '$server' is now online." else
                                "§cThe spigot server '$server' is now offline."
                            UtilsBungeeMain.instance.log("")
                            UtilsBungeeMain.instance.log("§6[RedisBungeeAPI] $logMsg")
                            UtilsBungeeMain.instance.log("")
                        }
                    }
                }, "mkUtils:BungeeAPI:Event:ConnectPlayer",
                "mkUtils:BungeeAPI:Event:KickPlayer",
                "mkUtils:BungeeAPI:Event:SendMsgToPlayer",
                "mkUtils:BungeeAPI:Event:ServerPowerAction"
            )
        }
    }

}