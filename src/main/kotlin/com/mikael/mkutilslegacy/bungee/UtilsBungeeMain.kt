package com.mikael.mkutilslegacy.bungee

import com.mikael.mkutilslegacy.api.UtilsManager
import com.mikael.mkutilslegacy.api.mkplugin.MKPlugin
import com.mikael.mkutilslegacy.api.mkplugin.MKPluginSystem
import com.mikael.mkutilslegacy.api.mkplugin.language.LangSystem
import com.mikael.mkutilslegacy.api.mkplugin.language.Translation
import com.mikael.mkutilslegacy.api.redis.RedisAPI
import com.mikael.mkutilslegacy.api.redis.RedisBungeeAPI
import com.mikael.mkutilslegacy.api.redis.RedisConnectionData
import com.mikael.mkutilslegacy.api.toTextComponent
import com.mikael.mkutilslegacy.bungee.api.utilsBungeeMain
import com.mikael.mkutilslegacy.bungee.command.BungeeVersionCommand
import com.mikael.mkutilslegacy.bungee.listener.RedisBungeeAPIListener
import net.eduard.api.lib.bungee.BungeeAPI
import net.eduard.api.lib.command.Command
import net.eduard.api.lib.config.Config
import net.eduard.api.lib.database.DBManager
import net.eduard.api.lib.database.HybridTypes
import net.eduard.api.lib.database.SQLManager
import net.eduard.api.lib.hybrid.BungeeServer
import net.eduard.api.lib.hybrid.Hybrid
import net.eduard.api.lib.kotlin.store
import net.eduard.api.lib.modules.Copyable
import net.eduard.api.lib.storage.StorageAPI
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * mkUtils (non-legacy for bungee) plugin main Bungee class.
 *
 * Special thanks to Eduard-- (*https://github.com/EduardMaster*).
 * This project uses some EduardAPI codes and methods.
 *
 * @author Mikael
 * @author KoddyDev
 * @author Eduard (EduardAPI)
 *
 * @see MKPlugin
 * @see utilsBungeeMain
 */
class UtilsBungeeMain : Plugin(), MKPlugin {
    companion object {
        lateinit var instance: UtilsBungeeMain
    }

    private var mySqlQueueUpdater: ScheduledTask? = null
    lateinit var config: Config

    init {
        Hybrid.instance = BungeeServer // EduardAPI
    }

    override fun onEnable() {
        instance = this@UtilsBungeeMain // Should be here
        val loadStart = System.currentTimeMillis()

        log(LangSystem.getText(Translation.LOADING_STARTING))
        UtilsManager.mkUtilsVersion = this.description.version
        prepareStorageAPI() // EduardAPI
        HybridTypes // {static} # Hybrid types - Load
        store<RedisConnectionData>()

        log(LangSystem.getText(Translation.LOADING_DIRECTORIES))
        config = Config(this, "config.yml")
        config.saveConfig()
        reloadConfigs() // x1
        reloadConfigs() // x2
        StorageAPI.updateReferences() // EduardAPI

        usingLanguage = config.getString("Region.Language").lowercase() // LangSystem
        regionFormatter = Locale.forLanguageTag(config.getString("Region.RegionFormatter").lowercase()) // LangSystem

        log(LangSystem.getText(Translation.LOADING_EXTRAS))
        prepareBasics()

        log(LangSystem.getText(Translation.LOADING_APIS))
        BungeeAPI.bungee.register(this) // EduardAPI

        log(LangSystem.getText(Translation.LOADING_SYSTEMS))
        prepareMySQL(); prepareRedis()

        // Commands
        BungeeVersionCommand().registerCommand(this)

        // Listeners
        RedisBungeeAPIListener().registerListener(this)

        val loadTime = System.currentTimeMillis() - loadStart
        log(LangSystem.getText(Translation.LOADING_COMPLETE).replace("%time_taken%", "$loadTime"))
        MKPluginSystem.registerMKPlugin(this@UtilsBungeeMain)

        ProxyServer.getInstance().scheduler.schedule(this, delay@{

            // Show MK Plugins
            log("§aLoaded MK Plugins:")
            for (mkPlugin in MKPluginSystem.loadedMKPlugins) {
                val mkProxyPl = mkPlugin.plugin as Plugin
                log(" §7▪ §e${mkProxyPl.description.name} v${mkProxyPl.description.version}")
            }

            // MySQL queue updater timer
            if (UtilsManager.sqlManager.hasConnection()) {
                mySqlQueueUpdater = ProxyServer.getInstance().scheduler.schedule(this, queueUpdater@{
                    if (!UtilsManager.sqlManager.hasConnection()) return@queueUpdater
                    UtilsManager.sqlManager.runChanges()
                }, 1, 1, TimeUnit.SECONDS)
            }
        }, 1, TimeUnit.SECONDS)
    }

    override fun onDisable() {
        log(LangSystem.getText(Translation.UNLOADING_SYSTEMS))

        BungeeAPI.controller.unregister() // EduardAPI
        RedisBungeeAPI.proxyServerPubSubThread?.interrupt()
        RedisAPI.jedisPool.destroy()
        mySqlQueueUpdater?.cancel()
        UtilsManager.dbManager.closeConnection()

        log(LangSystem.getText(Translation.UNLOADING_COMPLETE))
        MKPluginSystem.unregisterMKPlugin(this@UtilsBungeeMain)
    }

    private fun prepareRedis() {
        RedisAPI.managerData = config["Redis", RedisConnectionData::class.java]
        if (!RedisAPI.managerData.isEnabled) {
            log("§cRedis is not active on the config file. Some plugins and MK systems may not work correctly.")
            return
        }
        log("§eConnecting to Redis server...")
        RedisAPI.onEnablePrepareRedisAPI()
        if (!RedisAPI.isInitialized()) error("Cannot connect to Redis server")
        RedisAPI.useToSyncBungeePlayers = RedisAPI.managerData.syncBungeeDataUsingRedis
        if (RedisAPI.useToSyncBungeePlayers) {
            RedisBungeeAPI.proxyServerOnEnable()
        }
        log("§aConnected to Redis server!")
    }

    private fun prepareMySQL() {
        UtilsManager.sqlManager = SQLManager(config["Database", DBManager::class.java])
        if (!UtilsManager.sqlManager.dbManager.isEnabled) {
            log("§cThe MySQL is not active on the config file. Some plugins and MK systems may not work correctly.")
        }
        log("§eConnecting to MySQL database...")
        UtilsManager.dbManager.openConnection()
        if (!UtilsManager.sqlManager.hasConnection()) error("Cannot connect to MySQL database")
        log("§aConnected to MySQL database!")
    }

    private fun prepareStorageAPI() {
        StorageAPI.setDebug(false) // EduardAPI
        StorageAPI.startGson() // EduardAPI
    }

    private fun reloadConfigs() {
        config.add(
            "Database",
            DBManager(),
            "Config of MySQL database.",
            "All the plugins that use the mkUtilsProxy will use this MySQL database."
        )
        config.add(
            "Redis",
            RedisConnectionData(),
            "Config of Redis server.",
            "All the plugins that use the mkUtilsProxy will use this Redis server."
        )
        config.add(
            "RedisBungeeAPI.logSpigotServersPowerActions",
            false,
            "It'll send a message on Proxy server's console when a spigot server turn on/off."
        )
        config.add(
            "Region.Language",
            "en-us",
            "The language to be used by mkUtilsLegacy and all MK Plugins.",
            "Languages available at moment: 'en-us' & 'pt_br'. DON'T PUT A DIFFERENT VALUE!"
        )
        config.add(
            "Region.RegionFormatter",
            "US",
            "The Region Format to be used in Money formats.",
        )
        config.saveConfig()
    }

    private fun prepareBasics() {
        DBManager.setDebug(false) // EduardAPI
        Config.isDebug = false // EduardAPI
        Copyable.setDebug(false) // EduardAPI
        Command.MESSAGE_PERMISSION = "§cYou don't have permission to use this command." // EduardAPI
    }

    override var usingLanguage: String = "en-us" // Default always is 'en-us' (US English)
    override var regionFormatter: Locale = Locale.US // Default always is 'Locale.US' (US English)

    override fun log(vararg msg: String) {
        msg.forEach {
            ProxyServer.getInstance().console.sendMessage("§b[${systemName}] §f${it}".toTextComponent())
        }
    }

    override fun getPlugin(): Any {
        return this
    }

    override fun getSystemName(): String {
        return this.description.name
    }

    override fun getPluginFolder(): File {
        return this.dataFolder
    }

}