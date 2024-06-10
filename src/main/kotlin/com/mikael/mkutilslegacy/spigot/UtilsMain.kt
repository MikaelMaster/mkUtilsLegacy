package com.mikael.mkutilslegacy.spigot

import com.mikael.mkutilslegacy.api.UtilsManager
import com.mikael.mkutilslegacy.api.db.DBInfo
import com.mikael.mkutilslegacy.api.db.DatabaseAPI
import com.mikael.mkutilslegacy.api.db.DatabaseAPI.usingDBEngine
import com.mikael.mkutilslegacy.api.mkplugin.MKPlugin
import com.mikael.mkutilslegacy.api.mkplugin.MKPluginSystem
import com.mikael.mkutilslegacy.api.mkplugin.language.LangSystem
import com.mikael.mkutilslegacy.api.mkplugin.language.Translation
import com.mikael.mkutilslegacy.api.redis.RedisAPI
import com.mikael.mkutilslegacy.api.redis.RedisBungeeAPI
import com.mikael.mkutilslegacy.api.redis.RedisConnectionData
import com.mikael.mkutilslegacy.spigot.api.lib.hologram.listener.MineHologramListener
import com.mikael.mkutilslegacy.spigot.api.lib.menu.MenuSystem
import com.mikael.mkutilslegacy.spigot.api.npc.PlayerNPCAPI
import com.mikael.mkutilslegacy.spigot.api.npc.listener.NPCSystemListener
import com.mikael.mkutilslegacy.spigot.api.storable.LocationStorable
import com.mikael.mkutilslegacy.spigot.api.util.hooks.Vault
import com.mikael.mkutilslegacy.spigot.api.utilsMain
import com.mikael.mkutilslegacy.spigot.command.VersionCommand
import com.mikael.mkutilslegacy.spigot.listener.CustomEventsListener
import com.mikael.mkutilslegacy.spigot.listener.GeneralListener
import com.mikael.mkutilslegacy.spigot.listener.RedisBungeeAPIListener
import com.mikael.mkutilslegacy.spigot.task.AutoUpdateMenusTask
import com.mikael.mkutilslegacy.spigot.task.PlayerTargetAtPlayerTask
import net.eduard.api.core.BukkitReplacers
import net.eduard.api.lib.abstraction.Hologram
import net.eduard.api.lib.bungee.BungeeAPI
import net.eduard.api.lib.config.Config
import net.eduard.api.lib.database.BukkitTypes
import net.eduard.api.lib.database.DBManager
import net.eduard.api.lib.database.HybridTypes
import net.eduard.api.lib.hybrid.BukkitServer
import net.eduard.api.lib.hybrid.Hybrid
import net.eduard.api.lib.kotlin.store
import net.eduard.api.lib.manager.CommandManager
import net.eduard.api.lib.menu.Menu
import net.eduard.api.lib.modules.*
import net.eduard.api.lib.score.DisplayBoard
import net.eduard.api.lib.storage.StorageAPI
import net.eduard.api.lib.storage.storables.BukkitStorables
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.SimpleCommandMap
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.SimplePluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*

/**
 * mkUtilsLegacy plugin main Spigot class.
 *
 * Special thanks to [Eduard](https://github.com/EduardMaster).
 * This project uses some [EduardAPI/MineToolkit](https://github.com/EduardMaster/MineToolkit) extras and methods.
 *
 * @author Mikael
 * @author KoddyDev
 * @author Eduard (EduardAPI/MineToolkit)
 *
 * @see MKPlugin
 * @see utilsMain
 */
class UtilsMain : JavaPlugin(), MKPlugin, BukkitTimeHandler {
    companion object {
        lateinit var instance: UtilsMain
    }

    internal var scm: SimpleCommandMap? = null
    private var spm: SimplePluginManager? = null
    lateinit var config: Config

    init {
        Hybrid.instance = BukkitServer // EduardAPI
    }

    override fun onEnable() {
        instance = this@UtilsMain
        val loadStart = System.currentTimeMillis()

        log(LangSystem.getText(Translation.LOADING_STARTING))
        UtilsManager.mkUtilsVersion = this.description.version
        prepareStorageAPI(); prepareBasics() // EduardAPI
        HybridTypes // {static} # Hybrid types - Load
        BukkitTypes.register() // Bukkit types - Load
        store<RedisConnectionData>()

        Extra.FORMAT_DATE = SimpleDateFormat("dd/MM/yyyy") // EduardAPI
        Extra.FORMAT_DATETIME = SimpleDateFormat("dd/MM/yyyy HH:mm") // EduardAPI

        log(LangSystem.getText(Translation.LOADING_DIRECTORIES))
        config = Config(this@UtilsMain, "config.yml")
        config.saveConfig()
        reloadConfigs() // x1
        reloadConfigs() // x2
        StorageAPI.updateReferences() // EduardAPI

        usingLanguage = config.getString("Region.Language").lowercase() // LangSystem
        regionFormatter = Locale.forLanguageTag(config.getString("Region.RegionFormatter").lowercase()) // LangSystem

        log(LangSystem.getText(Translation.LOADING_EXTRAS))
        prepareTasks()

        log(LangSystem.getText(Translation.LOADING_APIS))
        MenuSystem.onEnable()
        Vault.onEnable()
        PlayerNPCAPI.onEnable()

        // BukkitBungeeAPI
        BukkitBungeeAPI.register(this) // EduardAPI
        BukkitBungeeAPI.requestCurrentServer() // EduardAPI
        BungeeAPI.bukkit.register(this) // EduardAPI

        log(LangSystem.getText(Translation.LOADING_SYSTEMS))
        prepareDatabaseAPI()
        prepareRedisAPI()
        prepareCommandMap()

        // Commands
        VersionCommand().registerCommand(this)

        // Listeners
        GeneralListener().registerListener(this)
        CustomEventsListener().registerListener(this)
        NPCSystemListener().registerListener(this) // PlayerNPCAPI
        MineHologramListener().registerListener(this) // MineHologram Extra
        if (RedisBungeeAPI.isEnabled) {
            RedisBungeeAPIListener().registerListener(this)
        }

        val loadTime = System.currentTimeMillis() - loadStart
        log(LangSystem.getText(Translation.LOADING_COMPLETE).replace("%time_taken%", "$loadTime"))
        MKPluginSystem.registerMKPlugin(this@UtilsMain)

        syncDelay(20) {
            log("§ePreparing MineReflect...")
            try {
                MineReflect.getVersion() // Prints the server (NMS) version
            } catch (ex: Exception) {
                Mine.console("§b[MineReflect] §cThe current version is not supported. Some custom features will not work, mkUtils will run with default Paper ones.")
            }

            log("§aLoaded MK Plugins:")
            for (mkPlugin in MKPluginSystem.getLoadedMKPlugins()) {
                log(" §7▪ §e${mkPlugin}")
            }
        }
    }

    override fun onDisable() {
        if (config.getBoolean("CustomKick.isEnabled")) {
            log(LangSystem.getText(Translation.UNLOADING_DISCONNECTING_PLAYERS))
            for (playerLoop in Bukkit.getOnlinePlayers()) {
                playerLoop.kickPlayer(config.getString("CustomKick.customKickMessage"))
            }
        }

        if (RedisBungeeAPI.isEnabled) {
            log(LangSystem.getText(Translation.UNLOADING_REDISBUNGEEAPI_DISABLING))
            RedisBungeeAPI.Spigot.updateSpigotServerState(false)
        }

        log(LangSystem.getText(Translation.UNLOADING_APIS))
        MenuSystem.onDisable()
        Vault.onDisable()
        PlayerNPCAPI.onDisable() // PlayerNPCAPI

        log(LangSystem.getText(Translation.UNLOADING_SYSTEMS))
        BungeeAPI.controller.unregister() // EduardAPI
        RedisBungeeAPI.Spigot.onDisableStopRedisSub()
        RedisAPI.unloadRedisAPI()
        DatabaseAPI.unloadDatabaseAPI()

        log(LangSystem.getText(Translation.UNLOADING_COMPLETE))
        MKPluginSystem.unregisterMKPlugin(this@UtilsMain)
    }

    private fun prepareCommandMap() {
        spm = server.pluginManager as SimplePluginManager
        var f: Field? = null
        try {
            f = SimplePluginManager::class.java.getDeclaredField("commandMap")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        f!!.isAccessible = true
        try {
            scm = f[spm] as SimpleCommandMap
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun prepareRedisAPI() {
        RedisAPI.managerData = config["Redis", RedisConnectionData::class.java]
        if (!RedisAPI.managerData.isEnabled) {
            log("§cRedis is not active on the config file. Some plugins and MK systems may not work correctly.")
            return
        }
        log("§eConnecting to Redis server...")
        RedisAPI.loadRedisAPI()
        if (!RedisAPI.isInitialized) error("Cannot connect to Redis server.")
        RedisAPI.useRedisBungeeAPI = RedisAPI.managerData.useRedisBungeeAPI
        if (RedisBungeeAPI.isEnabled) {
            RedisBungeeAPI.Spigot.onEnableStartRedisPubSub()
            syncDelay(1) {
                RedisBungeeAPI.Spigot.updateSpigotServerState(true)
            } // This will be executed after the server is done loading
        }
        log("§aConnected to Redis server!")
    }

    private fun prepareDatabaseAPI() {
        DatabaseAPI.usingDBInfo = config["Database", DBInfo::class.java]
        if (!DatabaseAPI.usingDBInfo.isEnabled) {
            log("§cDatabase (SQL) is not active on the config file. Some plugins and MK systems may not work correctly.")
            return
        }
        log("§eConnecting to ${usingDBEngine.display} DB...")
        if (DatabaseAPI.loadDatabaseAPI()) {
            log("§aConnected to ${usingDBEngine.display} DB!")
        }
    }

    private fun prepareStorageAPI() {
        StorageAPI.setDebug(false) // EduardAPI
        BukkitStorables.load() // EduardAPI

        // Storable Custom Objects
        StorageAPI.registerStorable(Location::class.java, LocationStorable())
        // StorageAPI.registerStorable(MineItemStorable::class.java, MineItemStorable())

        StorageAPI.startGson() // EduardAPI
    }

    private fun prepareBasics() {
        // mkUtils Menu System - Debug Mode
        Menu.isDebug = false // EduardAPI legacy Menu System - Debug Mode
        DBManager.setDebug(false) // EduardAPI
        Config.isDebug = false // EduardAPI
        Hologram.debug = false // EduardAPI
        CommandManager.debugEnabled = false // EduardAPI
        CommandManager.DEFAULT_USAGE_PREFIX = "§cUsage: " // EduardAPI
        CommandManager.DEFAULT_DESCRIPTION = "Description not defined." // EduardAPI
        Copyable.setDebug(false) // EduardAPI
        BukkitBungeeAPI.setDebuging(false) // EduardAPI
        DisplayBoard.colorFix = true // EduardAPI
        DisplayBoard.nameLimit = 40 // EduardAPI
        DisplayBoard.prefixLimit = 16 // EduardAPI
        DisplayBoard.suffixLimit = 16 // EduardAPI
    }

    private fun prepareTasks() {
        resetScoreboards() // EduardAPI
        BukkitReplacers() // EduardAPI
        if (config.getBoolean("MenuAPI.autoUpdateMenus")) {
            AutoUpdateMenusTask().syncTimer()
        }
        // Uses syncTimer to avoid concurrency issues:
        // (at org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList$Itr.next(UnsafeList.java:248))
        PlayerTargetAtPlayerTask().syncTimer()
    }

    private fun resetScoreboards() { // EduardAPI
        for (team in Mine.getMainScoreboard().teams) {
            team.unregister()
        }
        for (objective in Mine.getMainScoreboard().objectives) {
            objective.unregister()
        }
        for (player in Mine.getPlayers()) {
            player.scoreboard = Mine.getMainScoreboard()
            player.maxHealth = 20.0
            player.health = 20.0
            player.isHealthScaled = false
        }
    }

    private fun reloadConfigs() {
        // config.setHeader("mkUtils v${description.version} config file.") // It's bugged
        config.add(
            "Database",
            DBInfo(),
            "Config of SQL database."
        )
        config.add(
            "Redis",
            RedisConnectionData(),
            "Config of Redis server."
        )
        config.add(
            "RedisBungeeAPI.spigotServerName",
            "lobby-1",
            "The name of this spigot server. Should be the same as in the Proxy config file.",
            "This is used to identify the server in RedisBungeeAPI."
        )
        config.add(
            "Region.Language",
            "en-us",
            "The language to be used by mkUtilsLegacy and all MK Plugins.",
            "Languages available at moment: 'en-us' and 'pt-br'. DON'T PUT A DIFFERENT VALUE!"
        )
        config.add(
            "Region.RegionFormatter",
            "US",
            "The Region Format to be used in Money formats.",
            "Don't change it if you don't have sure what you're doing!"
        )
        config.add(
            "MenuAPI.autoUpdateMenus",
            true,
            "If true, mkUtils MenuAPI menus will auto update while open.",
        )
        config.add(
            "MenuAPI.autoUpdateTicks",
            60L,
            "Time to update players opened mkUtils MenuAPI menus.",
            "Values less than 20 may cause lag. 20 ticks = 1s."
        )
        config.add(
            "CustomKick.isEnabled",
            true,
            "If true, mkUtils will kick all online players with a custom message when the server is shutting down.",
        )
        config.add(
            "CustomKick.customKickMessage",
            "§cRestarting, we'll back soon!",
            "Kick message used to kick players on server shutdown."
        )
        config.saveConfig()
    }

    override var usingLanguage: String = "en-us" // Default always is 'en-us' (US English)
    override var regionFormatter: Locale = Locale.US // Default always is 'Locale.US' (US English)

    override fun log(vararg msg: String) {
        msg.forEach {
            Bukkit.getConsoleSender().sendMessage("§b[${systemName}] §f${it}")
        }
    }

    override fun getPlugin(): Any {
        return this
    }

    override fun getSystemName(): String {
        return this.name
    }

    override fun getPluginFolder(): File {
        return this.dataFolder
    }

    override fun getPluginConnected(): Plugin {
        return this
    }
}