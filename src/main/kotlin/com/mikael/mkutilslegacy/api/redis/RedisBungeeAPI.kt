package com.mikael.mkutilslegacy.api.redis

import com.mikael.mkutilslegacy.api.toTextComponent
import com.mikael.mkutilslegacy.bungee.UtilsBungeeMain
import com.mikael.mkutilslegacy.bungee.api.runBlock
import com.mikael.mkutilslegacy.spigot.UtilsMain
import com.mikael.mkutilslegacy.spigot.api.actionBar
import com.mikael.mkutilslegacy.spigot.api.runBlock
import com.mikael.mkutilslegacy.spigot.api.soundTP
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.json.JSONArray
import org.json.JSONObject
import redis.clients.jedis.JedisPubSub
import kotlin.concurrent.thread

/**
 * mkUtils [RedisBungeeAPI]
 *
 * It's very usefully over BungeeCord default messaging 'service'.
 *
 * @author Mikael
 * @see RedisAPI
 */
@Suppress("WARNINGS")
object RedisBungeeAPI {

    /**
     * @return True if the [RedisBungeeAPI] is enabled. Otherwise, false.
     */
    val isEnabled: Boolean get() = RedisAPI.isInitialized() && RedisAPI.useToSyncBungeePlayers

    // SPIGOT ONLY - Start

    /**
     * Returns this current spigot server name in mkUtils [RedisBungeeAPI] system.
     *
     * Should NOT be used in Proxy server side.
     *
     * @return The name of this server set in [UtilsMain.config] file.
     * @throws ClassCastException if used in Proxy server.
     */
    @JvmStatic
    val spigotServerName: String get() = UtilsMain.instance.config.getString("RedisBungeeAPI.spigotServerName")

    /**
     * Internal.
     */
    internal fun updateSpigotServerState(online: Boolean) {
        if (online) {
            RedisAPI.insertMap("mkUtils:BungeeAPI:Servers", mutableMapOf(spigotServerName to ""))
        } else {
            RedisAPI.mapDelete("mkUtils:BungeeAPI:Servers", spigotServerName)
        }
        RedisAPI.sendEvent(
            "mkUtils:BungeeAPI:Event:ServerPowerAction",
            "${spigotServerName};${if (online) "on" else "off"}"
        )
    }

    // SPIGOT ONLY - End

    /**
     * Please note that *servers will be returned as they're in the mkUtils Spigot Server Config File*.
     *
     * @return A list with all Spigot Servers online at this moment using the [RedisBungeeAPI].
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun getSpigotServers(): Set<String> {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        return RedisAPI.getMap("mkUtils:BungeeAPI:Servers").keys
    }

    /**
     * Returns the given [playerName] current Spigot Server name.
     *
     * @param playerName the player to get his current connected Spigot Server name.
     * @return The given [playerName] Spigot Server name. Can be null if the given [playerName] server is null.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun getPlayerServer(playerName: String): String? {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val playerNameLower = playerName.lowercase()
        return RedisAPI.getMap("mkUtils:BungeeAPI:Servers").entries.firstOrNull {
            it.value.split(";").filter { l -> l.isNotBlank() }.any { p -> p.lowercase() == playerNameLower }
        }?.key
    }

    /**
     * Returns all online players names logged-in in all online Spigot Severs.
     *
     * @return A set with all online players names. The set may be empty if there's no online player.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun getOnlinePlayers(): Set<String> {
        return getOnlinePlayersServers().keys
    }

    /**
     * Returns all servers and all online players in server.
     *
     * @return Map<ServerName, Set(PlayerName)> - with all online servers and players.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun getServersPlayers(): Map<String, Set<String>> {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val toReturn = mutableMapOf<String, MutableSet<String>>()
        val data = RedisAPI.getMap("mkUtils:BungeeAPI:Servers")
        for ((server, playersRaw) in data) {
            val players = playersRaw.split(";").filter { it.isNotBlank() }
            toReturn.getOrPut(server) { mutableSetOf() }.addAll(players)
        }
        return toReturn
    }

    /**
     * Returns all online players names on logged-in in the given [serverName].
     *
     * @param serverName the Spigot Server to get online players names.
     * @return A set with all online players names in the given Spigot Server.
     * This set may be empty if the given server doesn't exist or it's not online.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun getOnlinePlayers(serverName: String): Set<String> {
        return getServersPlayers()[serverName] ?: emptySet()
    }

    /**
     * Returns all online players and they current server.
     *
     * @return Map<PlayerName, ServerName> - with all online players and they current server.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun getOnlinePlayersServers(): Map<String, String> {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val toReturn = mutableMapOf<String, String>()
        val serversPlayers = getServersPlayers()
        for ((server, players) in serversPlayers) {
            for (player in players) {
                toReturn[player] = server
            }
        }
        return toReturn
    }

    /**
     * Returns all online players and spigot servers.
     *
     * @return Pair<Set(PlayerName), Set(ServerName)> - with all online players and spigot servers.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun getOnlinePlayersAndSpigotServers(): Pair<Set<String>, Set<String>> {
        return Pair(getOnlinePlayers(), getSpigotServers())
    }

    /**
     * Returns the online player amount of the given [serverName].
     *
     * @param serverName the Spigot Server to get online player amount.
     * @return The player amount ([Int]). If the given [serverName] is not online or does not exists, 0 will be returned.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun getPlayerAmount(serverName: String): Int {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        return getOnlinePlayers(serverName).size
    }

    /**
     * Returns the global player amount (all connected players amount) in all spigot servers.
     *
     * @return The global player amount ([Int]).
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun getGlobalPlayerAmount(): Int {
        return getOnlinePlayers().size
    }

    /**
     * Use to connect a player to a  specific Spigot Server.
     * It'll send a message to Proxy(s) to connect the given [playerName] to the given [serverName].
     * If the proxy that receive this message don't have the given player online, nothing will happen.
     *
     * @param playerName the player to connect.
     * @param serverName the server to connect the given [playerName].
     * @return True if the request has been sent with success. Otherwise, false.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun connectToServer(playerName: String, serverName: String): Boolean {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        return RedisAPI.sendEvent("mkUtils:BungeeAPI:Event:ConnectPlayer", "${playerName};${serverName}")
    }

    /**
     * Kicks a player from network.
     *
     * @param playerName the player to kick.
     * @param kickMessage the kick message to show.
     * @param bypassPerm a bypass permission. If the given [playerName] have this permission, he'll not be kicked.
     * @return True if the request has been sent with success. Otherwise, false.
     * @throws IllegalStateException if the given [kickMessage] contains the character ';'.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun kickPlayer(playerName: String, kickMessage: String, bypassPerm: String = "nullperm"): Boolean {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        if (kickMessage.contains(";")) error("kickMessage cannot contains ';' because of internal separator")
        if (bypassPerm.contains(";")) error("bypassPerm cannot contains ';' because of internal separator")
        return RedisAPI.sendEvent(
            "mkUtils:BungeeAPI:Event:KickPlayer",
            "${playerName};${kickMessage};${bypassPerm}"
        )
    }

    /**
     * It'll send a text component (on chat) to the given [playersToSend], through Redis.
     * The players will receive this message, regardless of the Proxy it is connected to.
     *
     * @param playersToSend a list of players to send the given [message].
     * @param message the message to send to the given [playersToSend].
     * @param neededPermission the permission that the player will NEED to have in order to receive the given [message].
     * If nothing is given, the player will receive the message ignoring the permission check.
     * @return True if the request has been sent with success. Otherwise, false.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun sendMessage(playersToSend: Set<String>, message: TextComponent, neededPermission: String = "nullperm"): Boolean {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        val json = JSONObject()
        json.put("playersToSend", JSONArray(playersToSend))
        json.put("message", ComponentSerializer.toString(message))
        json.put("neededPermission", neededPermission)
        return RedisAPI.sendEvent(
            "mkUtils:BungeeAPI:Event:SendMsgToPlayerList",
            json.toString()
        )
    }

    /**
     * @see sendMessage
     */
    fun sendMessage(playerName: String, message: TextComponent, neededPermission: String = "nullperm"): Boolean {
        return sendMessage(setOf(playerName), message, neededPermission)
    }

    /**
     * @see sendMessage
     */
    fun sendMessage(playersToSend: Set<String>, message: String, neededPermission: String = "nullperm"): Boolean {
        return sendMessage(playersToSend, TextComponent(message), neededPermission)
    }

    /**
     * @see sendMessage
     */
    fun sendMessage(playerName: String, message: String, neededPermission: String = "nullperm"): Boolean {
        return sendMessage(setOf(playerName), message, neededPermission)
    }

    /**
     * The spigot server with the given [playerName] will send the given [text] to him as an ActionBar.
     *
     * @param playerName the player to play the sound.
     * @param text the text to be shown in player's ActionBar.
     * @return True if the request has been sent with success. Otherwise, false.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun sendActionBar(playerName: String, text: String): Boolean {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        if (text.contains(";")) error("ActionBar text cannot contains ';' because of internal separator")
        return RedisAPI.sendEvent(
            "mkUtils:RedisBungeeAPI:Event:SendActionBarToPlayer",
            "${playerName};${text}"
        )
    }

    /**
     * @see playSound
     */
    fun playSound(playerName: String, bukkitSound: String, volume: Float = 2f, pitch: Float = 1f): Boolean {
        return playSound(setOf(playerName), bukkitSound, volume, pitch)
    }

    /**
     * The spigot server with the given [playerName] will play the given [bukkitSound] to him.
     *
     * @param playersName a [List] of players names to play the sound.
     * @param bukkitSound the sound to player. (Must be equivalent to the [Sound] enum)
     * @param volume the sound volume. Default: 2f.
     * @param pitch the sound pitch. Default: 1f.
     * @return True if the request has been sent with success. Otherwise, false.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun playSound(playersName: Set<String>, bukkitSound: String, volume: Float = 2f, pitch: Float = 1f): Boolean {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        return RedisAPI.sendEvent(
            "mkUtils:RedisBungeeAPI:Event:PlaySoundToPlayer",
            "${playersName.joinToString(",")};${bukkitSound};${volume};${pitch}"
        )
    }

    /**
     * Teleports the given [playerName] to the [targetName] (target as player in this case).
     *
     * @param playerName the player to teleport.
     * @param targetName the target to teleport the [targetName].
     * @param playTeleportSound if the sound of 'ENDERMAN_TELEPORT' should be player to the given [playerName] after teleport is complete.
     * @return True if the request has been sent with success. Otherwise, false.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun teleportPlayer(playerName: String, targetName: String, playTeleportSound: Boolean = true): Boolean {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        return RedisAPI.sendEvent(
            "mkUtils:RedisBungeeAPI:Event:TeleportPlayerToPlayer",
            "${playerName};${targetName};${playTeleportSound}"
        )
    }

    /**
     * Teleports the given [playerName] to the target (target as Location in this case).
     *
     * @param playerName the player to teleport.
     * @param world the world name of the location.
     * @param x the X location.
     * @param y the Y location.
     * @param z the Z location.
     * @param playTeleportSound if the sound of 'ENDERMAN_TELEPORT' should be player to the given [playerName] after teleport is complete.
     * @return True if the request has been sent with success. Otherwise, false.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun teleportPlayer(
        playerName: String,
        world: String,
        x: Double,
        y: Double,
        z: Double,
        playTeleportSound: Boolean = true
    ): Boolean {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        return RedisAPI.sendEvent(
            "mkUtils:RedisBungeeAPI:Event:TeleportPlayerToLocation",
            "${playerName};${world};${x};${y};${z};${playTeleportSound}"
        )
    }

    /**
     * Forces the given [playerName] to send a message in the chat. ([Player.chat])
     *
     * You can force the player to run commands with this, the message just needs to start with '/'.
     * Remember that the [msgToChat] cannot contains the character ';'.
     *
     * @param playerName the player to play the sound.
     * @param msgToChat the message to force player to send in chat.
     * @return True if the request has been sent with success. Otherwise, false.
     * @throws IllegalStateException if [isEnabled] is False.
     */
    fun sendChat(playerName: String, msgToChat: String): Boolean {
        if (!isEnabled) error("RedisBungeeAPI is not enabled.")
        if (msgToChat.contains(";")) error("ActionBar text cannot contains ';' because of internal separator")
        return RedisAPI.sendEvent(
            "mkUtils:RedisBungeeAPI:Event:SendChat",
            "${playerName};${msgToChat}"
        )
    }

    // REDIS SUBS SECTION BELOW

    // SPIGOT SUB - Start
    var bukkitServerPubSubThread: Thread? = null

    // mkUtils onEnable
    fun bukkitServerOnEnable() {
        bukkitServerPubSubThread = thread {
            RedisAPI.getExtraClient(RedisAPI.managerData).subscribe(
                object : JedisPubSub() {
                    override fun onMessage(channel: String, message: String) {
                        val data = message.split(";")
                        when (channel) {
                            "mkUtils:RedisBungeeAPI:Event:PlaySoundToPlayer" -> {
                                val players = data[0].split(",").filter { it.isNotEmpty() } // data[0] = playersName
                                val soundToPlay = Sound.valueOf(data[1].uppercase()) // data[1] = soundName
                                val volume = data[2].toFloat()
                                val pitch = data[3].toFloat()

                                players@for (playerName in players) {
                                    val player = Bukkit.getPlayer(playerName) ?: continue@players
                                    player.runBlock {
                                        player.playSound(player.location, soundToPlay, volume, pitch)
                                    }
                                }
                            }

                            "mkUtils:RedisBungeeAPI:Event:SendActionBarToPlayer" -> {
                                val player = Bukkit.getPlayer(data[0]) ?: return // data[0] = playerName
                                player.runBlock {
                                    player.actionBar(data[1]) // data[1] = message
                                }
                            }

                            "mkUtils:RedisBungeeAPI:Event:TeleportPlayerToPlayer" -> {
                                val player = Bukkit.getPlayer(data[0]) ?: return // data[0] = playerName
                                UtilsMain.instance.syncTask {
                                    player.runBlock {
                                        val target = Bukkit.getPlayer(data[1]) ?: return@runBlock
                                        player.teleport(target)
                                        if (data[2].toBoolean()) { // data[2] = playTeleportSound
                                            player.soundTP()
                                        }
                                    }
                                }
                            }

                            "mkUtils:RedisBungeeAPI:Event:TeleportPlayerToLocation" -> {
                                val player = Bukkit.getPlayer(data[0]) ?: return // data[0] = playerName
                                val worldName = data[1]
                                UtilsMain.instance.syncTask {
                                    player.runBlock {
                                        val world =
                                            Bukkit.getWorlds().firstOrNull { it.name.equals(worldName, true) } ?: error(
                                                "Given world $worldName is not loaded"
                                            )
                                        val loc =
                                            Location(world, data[2].toDouble(), data[3].toDouble(), data[4].toDouble())
                                        player.teleport(loc)
                                        if (data[5].toBoolean()) { // data[5] = playTeleportSound
                                            player.soundTP()
                                        }
                                    }
                                }
                            }

                            "mkUtils:RedisBungeeAPI:Event:SendChat" -> {
                                val player = Bukkit.getPlayer(data[0]) ?: return // data[0] = playerName
                                UtilsMain.instance.syncTask {
                                    player.runBlock {
                                        player.chat(data[1]) // data[1] = msgToChat
                                    }
                                }
                            }
                        }
                    }
                }, "mkUtils:RedisBungeeAPI:Event:PlaySoundToPlayer",
                "mkUtils:RedisBungeeAPI:Event:SendActionBarToPlayer",
                "mkUtils:RedisBungeeAPI:Event:TeleportPlayerToPlayer",
                "mkUtils:RedisBungeeAPI:Event:TeleportPlayerToLocation",
                "mkUtils:RedisBungeeAPI:Event:SendChat"
            )
        }
    }
    // SPIGOT SUB - End

    // PROXY SUB - Start
    var proxyServerPubSubThread: Thread? = null

    fun proxyServerOnEnable() {
        proxyServerPubSubThread = thread {
            RedisAPI.getExtraClient(RedisAPI.managerData).subscribe(
                object : JedisPubSub() {
                    override fun onMessage(channel: String, message: String) {
                        val data = message.split(";")
                        when (channel) {
                            "mkUtils:BungeeAPI:Event:ConnectPlayer" -> {
                                val player = ProxyServer.getInstance().getPlayer(data[0]) ?: return // data[0] = playerName
                                player.runBlock {
                                    val server = ProxyServer.getInstance().getServerInfo(data[1]) ?: return@runBlock // data[1] = serverName
                                    player.connect(server)
                                }
                            }

                            "mkUtils:BungeeAPI:Event:KickPlayer" -> {
                                val player = ProxyServer.getInstance().getPlayer(data[0]) ?: return // data[0] = playerName
                                player.runBlock {
                                    val bypassPerm = data[2]
                                    if (bypassPerm != "nullperm" && player.hasPermission(bypassPerm)) return@runBlock
                                    player.disconnect(data[1].toTextComponent()) // data[1] = kickMsg
                                }
                            }

                            "mkUtils:BungeeAPI:Event:SendMsgToPlayerList" -> {
                                val json = JSONObject(message)
                                val players = json.getJSONArray("playersToSend").toList() as List<String>
                                val message = TextComponent(*ComponentSerializer.parse(json.getString("message")).toList().toTypedArray())
                                val neededPermission = json.getString("neededPermission")

                                players@ for (playerName in players) {
                                    val player = ProxyServer.getInstance().getPlayer(playerName) ?: continue@players
                                    player.runBlock {
                                        if (neededPermission == "nullperm" || player.hasPermission(neededPermission)) {
                                            player.sendMessage(message)
                                        }
                                    }
                                }
                            }

                            "mkUtils:BungeeAPI:Event:ServerPowerAction" -> {
                                if (!UtilsBungeeMain.instance.config.getBoolean("RedisBungeeAPI.logSpigotServersPowerActions")) return
                                val server = data[0]
                                val newState = data[1]
                                val logMsg = if (newState == "on") "§aSpigot server '$server' is now online." else
                                    "§cSpigot server '$server' is now offline."
                                UtilsBungeeMain.instance.log(
                                    "",
                                    "§6[RedisBungeeAPI] $logMsg",
                                    ""
                                )
                            }
                        }
                    }
                }, "mkUtils:BungeeAPI:Event:ConnectPlayer",
                "mkUtils:BungeeAPI:Event:KickPlayer",
                "mkUtils:BungeeAPI:Event:SendMsgToPlayer",
                "mkUtils:BungeeAPI:Event:SendMsgToPlayerList",
                "mkUtils:BungeeAPI:Event:ServerPowerAction"
            )
        }
    }
    // PROXY SUB - End

}