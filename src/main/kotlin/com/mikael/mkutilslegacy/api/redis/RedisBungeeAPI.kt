package com.mikael.mkutilslegacy.api.redis

import com.mikael.mkutilslegacy.api.isProxyServer
import com.mikael.mkutilslegacy.api.redis.RedisBungeeAPI.RedisBungeeAPIChannel.*
import com.mikael.mkutilslegacy.api.redis.RedisBungeeAPI.RedisBungeeAPIKey.ONLINE_SPIGOT_SERVERS
import com.mikael.mkutilslegacy.api.redis.RedisBungeeAPI.RedisBungeeAPIKey.SPIGOT_SERVERS_ONLINE_PLAYERS
import com.mikael.mkutilslegacy.api.toTextComponent
import com.mikael.mkutilslegacy.bungee.api.runBlock
import com.mikael.mkutilslegacy.bungee.api.utilsBungeeMain
import com.mikael.mkutilslegacy.spigot.api.actionBar
import com.mikael.mkutilslegacy.spigot.api.runBlock
import com.mikael.mkutilslegacy.spigot.api.soundTP
import com.mikael.mkutilslegacy.spigot.api.utilsMain
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.json.JSONArray
import org.json.JSONObject
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import kotlin.concurrent.thread

/**
 * mkUtils [RedisBungeeAPI]
 *
 * This is a class that provides a set of functions to sync data between Spigot and BungeeCord servers using Redis.
 * It's important to note that this class is a part of the [RedisAPI] and it's only available
 * if the [RedisAPI] is enabled and the [RedisAPI.useRedisBungeeAPI] is *true*.
 *
 * @author Mikael
 * @see RedisAPI
 */
@Suppress("WARNINGS", "UNCHECKED_CAST")
object RedisBungeeAPI {

    // Properties - Start
    /**
     * @return True if the [RedisBungeeAPI] is enabled. Otherwise, false.
     */
    val isEnabled: Boolean get() = RedisAPI.isInitialized && RedisAPI.useRedisBungeeAPI
    // Properties - End

    // Internal methods - Start
    private fun debug(msg: String) {
        if (!isEnabled || !RedisAPI.managerData.debugRedisBungeeAPI) return
        if (isProxyServer) {
            utilsBungeeMain.log("§e[RedisBungeeAPI] §6[DEBUG] §f$msg")
        } else {
            utilsMain.log("§e[RedisBungeeAPI] §6[DEBUG] §f$msg")
        }
    }
    // Internal methods - End

    enum class RedisBungeeAPIKey(val key: String) {
        ONLINE_SPIGOT_SERVERS("mkUtils_RedisBungeeAPI_OnlineSpigotServers"),
        SPIGOT_SERVERS_ONLINE_PLAYERS("mkUtils_RedisBungeeAPI_OnlineSpigotServers_Players"),
    }

    fun getOnlineSpigotServers(): Set<String> {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val start = System.currentTimeMillis()
        RedisAPI.jedisPool.resource.use { resource ->
            val resOnlineSpigotServers = resource.smembers(ONLINE_SPIGOT_SERVERS.key)
            debug("getOnlineSpigotServers() took ${System.currentTimeMillis() - start}ms.")
            return resOnlineSpigotServers
        }
    }

    fun getOnlineServersPlayers(): Map<String, Set<String>> {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val start = System.currentTimeMillis()
        RedisAPI.jedisPool.resource.use { resource ->
            val pipeline = resource.pipelined()
            val resOnlineSpigotServers = pipeline.smembers(ONLINE_SPIGOT_SERVERS.key)
            pipeline.sync()
            val servers = resOnlineSpigotServers.get()
            val toReturn = mutableMapOf<String, Set<String>>()
            for (server in servers) {
                pipeline.smembers("${SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${server}")
            }
            val serversPlayers = pipeline.syncAndReturnAll() as List<Set<String>>
            for ((index, server) in servers.withIndex()) {
                toReturn[server] = serversPlayers[index]
            }
            debug("getOnlineServersPlayers() took ${System.currentTimeMillis() - start}ms.")
            return toReturn
        }
    }

    fun getPlayerServer(playerName: String): String? {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val start = System.currentTimeMillis()
        RedisAPI.jedisPool.resource.use { resource ->
            val pipeline = resource.pipelined()
            val resOnlineServers = pipeline.smembers(ONLINE_SPIGOT_SERVERS.key)
            pipeline.sync()
            val onlineServers = resOnlineServers.get()
            for (server in onlineServers) {
                val resPlayerServer =
                    pipeline.sismember("${SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${server}", playerName.lowercase())
            }
            val playerServer = (pipeline.syncAndReturnAll() as List<Boolean>)
                .mapIndexed { index, isMember -> if (isMember) onlineServers.elementAt(index) else null }
                .firstOrNull { it != null }
            debug("getPlayerServer(playerName: String) took ${System.currentTimeMillis() - start}ms.")
            return playerServer
        }
    }

    fun isPlayerOnline(playerName: String): Boolean {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val start = System.currentTimeMillis()
        RedisAPI.jedisPool.resource.use { resource ->
            val pipeline = resource.pipelined()
            val resOnlineServers = pipeline.smembers(ONLINE_SPIGOT_SERVERS.key)
            pipeline.sync()
            val onlineServers = resOnlineServers.get()
            for (server in onlineServers) {
                val resPlayerServer =
                    pipeline.sismember("${SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${server}", playerName.lowercase())
            }
            val isOnline = (pipeline.syncAndReturnAll() as List<Boolean>).any { it }
            debug("isPlayerOnline(playerName: String) took ${System.currentTimeMillis() - start}ms.")
            return isOnline
        }
    }

    fun getOnlinePlayers(serverName: String): Set<String>? {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val start = System.currentTimeMillis()
        RedisAPI.jedisPool.resource.use { resource ->
            val pipeline = resource.pipelined()
            val resExistsServer = pipeline.exists("${SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${serverName}")
            val resServerPlayers = pipeline.smembers("${SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${serverName}")
            pipeline.sync()
            debug("getOnlinePlayers(serverName: String) took ${System.currentTimeMillis() - start}ms.")
            if (!resExistsServer.get()) return null
            return resServerPlayers.get()
        }
    }

    fun getOnlinePlayersCount(serverName: String): Int? {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val start = System.currentTimeMillis()
        RedisAPI.jedisPool.resource.use { resource ->
            val pipeline = resource.pipelined()
            val resExistsServer = pipeline.exists("${SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${serverName}")
            val resServerPlayers = pipeline.scard("${SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${serverName}")
            pipeline.sync()
            if (!resExistsServer.get()) return null
            debug("getOnlinePlayersCount(serverName: String) took ${System.currentTimeMillis() - start}ms.")
            return resServerPlayers.get().toInt()
        }
    }

    fun getOnlinePlayersServers(): Map<String, String> {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val start = System.currentTimeMillis()
        RedisAPI.jedisPool.resource.use { resource ->
            val pipeline = resource.pipelined()
            val resOnlineSpigotServers = pipeline.smembers(ONLINE_SPIGOT_SERVERS.key)
            pipeline.sync()
            val servers = resOnlineSpigotServers.get()
            val toReturn = mutableMapOf<String, String>()
            for (server in servers) {
                pipeline.smembers("${SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${server}")
            }
            val serversPlayers = pipeline.syncAndReturnAll() as List<Set<String>>
            for ((index, server) in servers.withIndex()) {
                serversPlayers[index].forEach { playerName ->
                    toReturn[playerName] = server
                }
            }
            debug("getOnlinePlayersServers() took ${System.currentTimeMillis() - start}ms.")
            return toReturn
        }
    }

    fun getOnlinePlayers(): Set<String> {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val start = System.currentTimeMillis()
        RedisAPI.jedisPool.resource.use { resource ->
            val pipeline = resource.pipelined()
            val resOnlineSpigotServers = pipeline.smembers(ONLINE_SPIGOT_SERVERS.key)
            pipeline.sync()
            val servers = resOnlineSpigotServers.get()
            val toReturn = mutableSetOf<String>()
            for (server in servers) {
                pipeline.smembers("${SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${server}")
            }
            val serversPlayers = pipeline.syncAndReturnAll() as List<Set<String>>
            for (serverPlayers in serversPlayers) {
                toReturn.addAll(serverPlayers)
            }
            debug("getOnlinePlayers() took ${System.currentTimeMillis() - start}ms.")
            return toReturn
        }
    }

    fun getOnlinePlayersCount(): Int {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val start = System.currentTimeMillis()
        RedisAPI.jedisPool.resource.use { resource ->
            val pipeline = resource.pipelined()
            val resOnlineSpigotServers = pipeline.smembers(ONLINE_SPIGOT_SERVERS.key)
            pipeline.sync()
            val servers = resOnlineSpigotServers.get()
            var count = 0
            for (server in servers) {
                pipeline.scard("${SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${server}")
            }
            val serversPlayersCount = pipeline.syncAndReturnAll() as List<Long>
            for (serverPlayersCount in serversPlayersCount) {
                count += serverPlayersCount.toInt()
            }
            debug("getOnlinePlayersCount() took ${System.currentTimeMillis() - start}ms.")
            return count
        }
    }

    data class OnlinePlayersAndSpigotServersResult(val onlinePlayers: Set<String>, val onlineSpigotServers: Set<String>)

    fun getOnlinePlayersAndSpigotServers(): OnlinePlayersAndSpigotServersResult {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val start = System.currentTimeMillis()
        RedisAPI.jedisPool.resource.use { resource ->
            val pipeline = resource.pipelined()
            val resOnlineSpigotServers = pipeline.smembers(ONLINE_SPIGOT_SERVERS.key)
            pipeline.sync()
            val servers = resOnlineSpigotServers.get()
            val toReturnPlayers = mutableSetOf<String>()
            val toReturnServers = mutableSetOf<String>()
            for (server in servers) {
                pipeline.smembers("${SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${server}")
            }
            val serversPlayers = pipeline.syncAndReturnAll() as List<Set<String>>
            for ((index, server) in servers.withIndex()) {
                toReturnServers.add(server)
                toReturnPlayers.addAll(serversPlayers[index])
            }
            debug("getOnlinePlayersAndSpigotServers() took ${System.currentTimeMillis() - start}ms.")
            return OnlinePlayersAndSpigotServersResult(toReturnPlayers, toReturnServers)
        }
    }

    enum class RedisBungeeAPIChannel(val ch: String) {
        // Spigot -> BungeeCord (Received in BungeeCord)
        CONNECT_PLAYER("mkUtils:RedisBungeeAPI:BungeeEvent:ConnectPlayer"),
        KICK_PLAYER("mkUtils:RedisBungeeAPI:BungeeEvent:KickPlayer"),
        SEND_MSG_TO_PLAYER_LIST("mkUtils:RedisBungeeAPI:BungeeEvent:SendMsgToPlayerList"),
        SEND_PROXY_CHAT("mkUtils:RedisBungeeAPI:BungeeEvent:SendProxyChat"),
        DISPATCH_PROXY_CMD("mkUtils:RedisBungeeAPI:BungeeEvent:DispatchProxyCmd"),
        SEND_TITLE_TO_PLAYER_LIST("mkUtils:RedisBungeeAPI:BungeeEvent:SendTitleToPlayerList"),
        LOG_SPIGOT_SERVER_POWER_ACTION("mkUtils:RedisBungeeAPI:BungeeEvent:ServerPowerAction"),

        // BungeeCord -> Spigot (Received in Spigot)
        PLAY_SOUND_TO_PLAYER("mkUtils:RedisBungeeAPI:SpigotEvent:PlaySoundToPlayer"),
        SEND_ACTION_BAR_TO_PLAYER("mkUtils:RedisBungeeAPI:SpigotEvent:SendActionBarToPlayer"),
        TELEPORT_PLAYER_TO_PLAYER("mkUtils:RedisBungeeAPI:SpigotEvent:TeleportPlayerToPlayer"),
        TELEPORT_PLAYER_TO_LOCATION("mkUtils:RedisBungeeAPI:SpigotEvent:TeleportPlayerToLocation"),
        SEND_CHAT("mkUtils:RedisBungeeAPI:SpigotEvent:SendChat")
    }

    fun connectToServer(playerName: String, serverName: String) {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        RedisAPI.sendEvent(
            CONNECT_PLAYER.ch,
            JSONObject()
                .put("playerName", playerName)
                .put("serverName", serverName)
                .toString()
        )
    }

    fun kickPlayer(playerName: String, kickMessage: String, bypassPerm: String = "nullperm") {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        RedisAPI.sendEvent(
            KICK_PLAYER.ch,
            JSONObject()
                .put("playerName", playerName)
                .put("kickMessage", kickMessage)
                .put("bypassPerm", bypassPerm)
                .toString()
        )
    }

    fun sendMessage(playersToSend: Set<String>, vararg message: BaseComponent, neededPermission: String = "nullperm") {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        RedisAPI.sendEvent(
            SEND_MSG_TO_PLAYER_LIST.ch,
            JSONObject()
                .put("playersToSend", JSONArray(playersToSend))
                .put("message", ComponentSerializer.toString(*message))
                .put("neededPermission", neededPermission)
                .toString()
        )
    }

    fun sendMessage(playersToSend: Set<String>, message: TextComponent, neededPermission: String = "nullperm") {
        sendMessage(playersToSend, *listOf(message).toTypedArray(), neededPermission = neededPermission)
    }

    fun sendMessage(playerName: String, message: TextComponent, neededPermission: String = "nullperm") {
        sendMessage(setOf(playerName), message, neededPermission)
    }

    fun sendMessage(playersToSend: Set<String>, message: String, neededPermission: String = "nullperm") {
        sendMessage(playersToSend, message.toTextComponent(), neededPermission)
    }

    fun sendMessage(playerName: String, message: String, neededPermission: String = "nullperm") {
        sendMessage(setOf(playerName), message, neededPermission)
    }

    fun sendActionBar(playerName: String, text: String) {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        RedisAPI.sendEvent(
            SEND_ACTION_BAR_TO_PLAYER.ch,
            JSONObject()
                .put("playerName", playerName)
                .put("text", text)
                .toString()
        )
    }

    fun sendTitle(
        playersName: Set<String>,
        title: String,
        subtitle: String,
        fadeIn: Int = 10,
        stay: Int = 20 * 3,
        fadeOut: Int = 10
    ) {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        RedisAPI.sendEvent(
            SEND_TITLE_TO_PLAYER_LIST.ch,
            JSONObject()
                .put("playersName", JSONArray(playersName))
                .put("title", title)
                .put("subtitle", subtitle)
                .put("fadeIn", fadeIn)
                .put("stay", stay)
                .put("fadeOut", fadeOut)
                .toString() // "${playersName.joinToString(",")};${title};${subtitle};${fadeIn};${stay};${fadeOut}"
        )
    }

    fun sendTitle(
        playerName: String,
        title: String,
        subtitle: String,
        fadeIn: Int = 10,
        stay: Int = 20 * 3,
        fadeOut: Int = 10
    ) {
        sendTitle(setOf(playerName), title, subtitle, fadeIn, stay, fadeOut)
    }

    fun playSound(playersName: Set<String>, bukkitSound: String, volume: Float = 2f, pitch: Float = 1f) {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        RedisAPI.sendEvent(
            PLAY_SOUND_TO_PLAYER.ch,
            JSONObject()
                .put("playersName", JSONArray(playersName))
                .put("bukkitSound", bukkitSound)
                .put("volume", volume)
                .put("pitch", pitch)
                .toString() // "${playersName.joinToString(",")};${bukkitSound};${volume};${pitch}"
        )
    }

    fun playSound(playerName: String, bukkitSound: String, volume: Float = 2f, pitch: Float = 1f) {
        playSound(setOf(playerName), bukkitSound, volume, pitch)
    }

    fun teleportPlayer(playerName: String, targetName: String, playTeleportSound: Boolean = true) {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        RedisAPI.sendEvent(
            TELEPORT_PLAYER_TO_PLAYER.ch,
            JSONObject()
                .put("playerName", playerName)
                .put("targetName", targetName)
                .put("playTeleportSound", playTeleportSound)
                .toString() // "${playerName};${targetName};${playTeleportSound}"
        )
    }

    fun teleportPlayer(
        playerName: String,
        world: String,
        x: Double,
        y: Double,
        z: Double,
        playTeleportSound: Boolean = true
    ) {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        RedisAPI.sendEvent(
            TELEPORT_PLAYER_TO_LOCATION.ch,
            JSONObject()
                .put("playerName", playerName)
                .put("world", world)
                .put("x", x)
                .put("y", y)
                .put("z", z)
                .put("playTeleportSound", playTeleportSound)
                .toString() // "${playerName};${world};${x};${y};${z};${playTeleportSound}"
        )
    }

    fun sendChat(playerName: String, msgToChat: String) {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        RedisAPI.sendEvent(
            SEND_CHAT.ch,
            JSONObject()
                .put("playerName", playerName)
                .put("msgToChat", msgToChat)
                .toString()
        )
    }

    fun sendProxyChat(proxiedPlayerName: String, msgToChat: String) {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        RedisAPI.sendEvent(
            SEND_PROXY_CHAT.ch,
            JSONObject()
                .put("playerName", proxiedPlayerName)
                .put("msgToChat", msgToChat)
                .toString()
        )
    }

    fun dispatchProxyCmd(proxiedPlayerName: String, proxyCmd: String) {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val finalCmd = proxyCmd.removePrefix("/")
        RedisAPI.sendEvent(
            DISPATCH_PROXY_CMD.ch,
            JSONObject()
                .put("playerName", proxiedPlayerName)
                .put("proxyCmd", finalCmd)
                .toString()
        )
    }

    /**
     * Methods inside [RedisBungeeAPI.Spigot] can only be used in Spigot servers.
     * If any method/val here is called in a BungeeCord server, it'll throw an [IllegalStateException].
     */
    object Spigot {
        @JvmStatic
        val spigotServerName: String
            get() {
                if (isProxyServer) error("This method can only be used in Spigot servers.")
                return utilsMain.config.getString("RedisBungeeAPI.spigotServerName")
            }

        internal fun updateSpigotServerState(online: Boolean) {
            RedisAPI.jedisPool.resource.use { resource ->
                val pipeline = resource.pipelined()
                pipeline.del("${SPIGOT_SERVERS_ONLINE_PLAYERS.key}_${spigotServerName}")
                if (online) {
                    pipeline.sadd(ONLINE_SPIGOT_SERVERS.key, spigotServerName)
                } else {
                    pipeline.srem(ONLINE_SPIGOT_SERVERS.key, spigotServerName)
                }
                pipeline.sync()
            }
            RedisAPI.sendEvent(
                LOG_SPIGOT_SERVER_POWER_ACTION.ch,
                JSONObject()
                    .put("server", spigotServerName)
                    .put("online", online)
                    .toString() // "${spigotServerName};${if (online) "on" else "off"}"
            )
        }

        // Spigot RedisPubSub - Start
        private var redisPubSubThread: Thread? = null
        private var redisPubSubJedisClient: Jedis? = null

        internal fun onEnableStartRedisPubSub() {
            redisPubSubJedisClient = RedisAPI.getExtraClient(RedisAPI.managerData)
            redisPubSubThread = thread {
                redisPubSubJedisClient!!.subscribe(
                    object : JedisPubSub() {
                        override fun onMessage(channel: String, message: String) {
                            val json = JSONObject(message)
                            when (channel) {
                                PLAY_SOUND_TO_PLAYER.ch -> {
                                    val players = json.getJSONArray("playersName").map { it.toString() }
                                    val sound = Sound.valueOf(json.getString("bukkitSound"))
                                    val volume = json.getFloat("volume")
                                    val pitch = json.getFloat("pitch")
                                    players@ for (playerName in players) {
                                        val player = Bukkit.getPlayer(playerName) ?: continue@players
                                        player.runBlock {
                                            player.playSound(player.location, sound, volume, pitch)
                                        }
                                    }
                                }

                                SEND_ACTION_BAR_TO_PLAYER.ch -> {
                                    val player = Bukkit.getPlayer(json.getString("playerName")) ?: return
                                    player.runBlock {
                                        player.actionBar(json.getString("text"))
                                    }
                                }

                                TELEPORT_PLAYER_TO_PLAYER.ch -> {
                                    val player = Bukkit.getPlayer(json.getString("playerName")) ?: return
                                    val target = Bukkit.getPlayer(json.getString("targetName")) ?: return
                                    utilsMain.syncTask {
                                        player.runBlock {
                                            player.teleport(target)
                                            if (json.getBoolean("playTeleportSound")) {
                                                player.soundTP()
                                            }
                                        }
                                    }
                                }

                                TELEPORT_PLAYER_TO_LOCATION.ch -> {
                                    val player = Bukkit.getPlayer(json.getString("playerName")) ?: return
                                    val worldName = json.getString("world")
                                    utilsMain.syncTask {
                                        player.runBlock {
                                            val world = Bukkit.getWorld(worldName)
                                                ?: error("Given world $worldName is not loaded.")
                                            val loc = Location(
                                                world,
                                                json.getDouble("x"),
                                                json.getDouble("y"),
                                                json.getDouble("z")
                                            )
                                            player.teleport(loc)
                                            if (json.getBoolean("playTeleportSound")) {
                                                player.soundTP()
                                            }
                                        }
                                    }
                                }

                                SEND_CHAT.ch -> {
                                    val player = Bukkit.getPlayer(json.getString("playerName")) ?: return
                                    utilsMain.syncTask {
                                        player.runBlock {
                                            player.chat(json.getString("msgToChat"))
                                        }
                                    }
                                }
                            }
                        }
                    },
                    PLAY_SOUND_TO_PLAYER.ch,
                    SEND_ACTION_BAR_TO_PLAYER.ch,
                    TELEPORT_PLAYER_TO_PLAYER.ch,
                    TELEPORT_PLAYER_TO_LOCATION.ch,
                    SEND_CHAT.ch
                )
            }
        }

        internal fun onDisableStopRedisSub() {
            try {
                redisPubSubThread?.interrupt()
                // redisPubSubJedisClient?.close() // This may cause an exception because the thread is still running
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                redisPubSubThread = null
                redisPubSubJedisClient = null
            }
        }
        // Spigot RedisPubSub - End
    }


    /**
     * Methods inside [RedisBungeeAPI.Bungee] can only be used in BungeeCord servers.
     * If any method/val here is called in a Spigot server, it'll throw an [IllegalStateException].
     */
    public object Bungee {
        // Bungee RedisPubSub - Start
        private var redisPubSubThread: Thread? = null
        private var redisPubSubJedisClient: Jedis? = null

        internal fun onEnableStartRedisPubSub() {
            redisPubSubJedisClient = RedisAPI.getExtraClient(RedisAPI.managerData)
            redisPubSubThread = thread {
                redisPubSubJedisClient!!.subscribe(
                    object : JedisPubSub() {
                        override fun onMessage(channel: String, message: String) {
                            val json = JSONObject(message)
                            when (channel) {
                                CONNECT_PLAYER.ch -> {
                                    val player =
                                        ProxyServer.getInstance().getPlayer(json.getString("playerName")) ?: return
                                    player.runBlock {
                                        val serverName = json.getString("serverName")
                                        player.connect(ProxyServer.getInstance().getServerInfo(serverName))
                                    }
                                }

                                KICK_PLAYER.ch -> {
                                    val player =
                                        ProxyServer.getInstance().getPlayer(json.getString("playerName")) ?: return
                                    player.runBlock {
                                        val bypassPerm = json.getString("bypassPerm")
                                        if (bypassPerm != "nullperm" && player.hasPermission(bypassPerm)) return@runBlock
                                        val kickMessage = json.getString("kickMessage")
                                        player.disconnect(kickMessage.toTextComponent())
                                    }
                                }

                                SEND_MSG_TO_PLAYER_LIST.ch -> {
                                    val players = json.getJSONArray("playersToSend").map { it.toString() }
                                    val message = ComponentSerializer.parse(json.getString("message"))
                                    val neededPermission = json.getString("neededPermission")
                                    players@ for (playerName in players) {
                                        val player = ProxyServer.getInstance().getPlayer(playerName) ?: continue@players
                                        if (neededPermission != "nullperm" && !player.hasPermission(neededPermission)) continue@players
                                        player.sendMessage(*message)
                                    }
                                }

                                SEND_PROXY_CHAT.ch -> {
                                    val player =
                                        ProxyServer.getInstance().getPlayer(json.getString("playerName")) ?: return
                                    player.runBlock {
                                        val msgToChat = json.getString("msgToChat")
                                        player.chat(msgToChat)
                                    }
                                }

                                DISPATCH_PROXY_CMD.ch -> {
                                    val player =
                                        ProxyServer.getInstance().getPlayer(json.getString("playerName")) ?: return
                                    player.runBlock {
                                        val proxyCmd = json.getString("proxyCmd")
                                        ProxyServer.getInstance().pluginManager.dispatchCommand(player, proxyCmd)
                                    }
                                }

                                SEND_TITLE_TO_PLAYER_LIST.ch -> {
                                    val players = json.getJSONArray("playersName").map { it.toString() }
                                    val title = ProxyServer.getInstance().createTitle()
                                        .title(json.getString("title").toTextComponent())
                                        .subTitle(json.getString("subtitle").toTextComponent())
                                        .fadeIn(json.getInt("fadeIn"))
                                        .stay(json.getInt("stay"))
                                        .fadeOut(json.getInt("fadeOut"))
                                    players@ for (playerName in players) {
                                        val player = ProxyServer.getInstance().getPlayer(playerName) ?: continue@players
                                        player.runBlock {
                                            title.send(player)
                                        }
                                    }
                                }

                                LOG_SPIGOT_SERVER_POWER_ACTION.ch -> {
                                    if (!utilsBungeeMain.config.getBoolean("RedisBungeeAPI.logSpigotServersPowerActions")) return
                                    val server = json.getString("server")
                                    val online = json.getBoolean("online")
                                    utilsBungeeMain.log(
                                        if (online) "§e[RedisBungeeAPI] §aSpigot server '$server' updated its status to online."
                                        else "§e[RedisBungeeAPI] §cSpigot server '$server' updated its status to offline."
                                    )
                                }
                            }
                        }
                    },
                    CONNECT_PLAYER.ch,
                    KICK_PLAYER.ch,
                    SEND_MSG_TO_PLAYER_LIST.ch,
                    SEND_PROXY_CHAT.ch,
                    DISPATCH_PROXY_CMD.ch,
                    SEND_TITLE_TO_PLAYER_LIST.ch,
                    LOG_SPIGOT_SERVER_POWER_ACTION.ch
                )
            }
        }

        internal fun onDisableStopRedisSub() {
            try {
                redisPubSubThread?.interrupt()
                // redisPubSubJedisClient?.close() // This may cause an exception because the thread is still running
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                redisPubSubThread = null
                redisPubSubJedisClient = null
            }
        }
        // Bungee RedisPubSub - End
    }

}