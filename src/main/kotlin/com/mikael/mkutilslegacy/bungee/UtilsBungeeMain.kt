package com.mikael.mkutilslegacy.bungee

import com.mikael.mkutilslegacy.api.UtilsManager
import com.mikael.mkutilslegacy.api.mkplugin.MKPlugin
import com.mikael.mkutilslegacy.api.mkplugin.MKPluginSystem
import com.mikael.mkutilslegacy.api.redis.RedisAPI
import com.mikael.mkutilslegacy.api.redis.RedisBungeeAPI
import com.mikael.mkutilslegacy.api.redis.RedisConnectionData
import com.mikael.mkutilslegacy.api.toTextComponent
import com.mikael.mkutilslegacy.api.utilsmanager
import com.mikael.mkutilslegacy.bungee.command.BungeeVersionCommand
import com.mikael.mkutilslegacy.bungee.listener.BungeeGeneralListener
import net.eduard.api.lib.bungee.BungeeAPI
import net.eduard.api.lib.command.Command
import net.eduard.api.lib.config.Config
import net.eduard.api.lib.database.DBManager
import net.eduard.api.lib.database.HybridTypes
import net.eduard.api.lib.database.SQLManager
import net.eduard.api.lib.hybrid.BungeeServer
import net.eduard.api.lib.hybrid.Hybrid
import net.eduard.api.lib.kotlin.register
import net.eduard.api.lib.kotlin.resolvePut
import net.eduard.api.lib.kotlin.store
import net.eduard.api.lib.modules.Copyable
import net.eduard.api.lib.storage.StorageAPI
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.io.File
import java.util.concurrent.TimeUnit

class UtilsBungeeMain : Plugin(), MKPlugin {
    companion object {
        lateinit var instance: UtilsBungeeMain
    }

    private var mySqlQueueUpdater: ScheduledTask? = null
    lateinit var manager: UtilsManager
    lateinit var config: Config

    init {
        Hybrid.instance = BungeeServer // EduardAPI
    }

    override fun onEnable() {
        instance = this@UtilsBungeeMain
        val start = System.currentTimeMillis()
        manager = resolvePut(UtilsManager()) // Should be here

        log("§eStarting loading...")
        manager.mkUtilsVersion = this.description.version
        prepareStorageAPI() // EduardAPI
        HybridTypes // {static} # Hybrid types - Load
        store<RedisConnectionData>()

        log("§eLoading directories...")
        config = Config(this, "config.yml")
        config.saveConfig()
        reloadConfigs() // x1
        reloadConfigs() // x2
        StorageAPI.updateReferences() // EduardAPI

        log("§eLoading extras...")
        prepareBasics()

        log("§eLoading APIs...")
        BungeeAPI.bungee.register(this) // EduardAPI

        log("§eLoading systems...")
        prepareMySQL(); prepareRedis()

        // Commands
        BungeeVersionCommand().register(this)

        // Listeners
        BungeeGeneralListener().register(this)

        val endTime = System.currentTimeMillis() - start
        log("§aPlugin loaded with success! (Time taken: §f${endTime}ms§a)"); MKPluginSystem.loadedMKPlugins.add(this@UtilsBungeeMain)

        ProxyServer.getInstance().scheduler.schedule(this, delay@{

            // Show MK Plugins
            log("§aLoaded MK Plugins:")
            for (mkPlugin in MKPluginSystem.loadedMKPlugins) {
                val mkProxyPl = mkPlugin.plugin as Plugin
                log(" §7▪ §e${mkProxyPl.description.name} v${mkProxyPl.description.version}")
            }

            // MySQL queue updater timer
            if (utilsmanager.sqlManager.hasConnection()) {
                mySqlQueueUpdater = ProxyServer.getInstance().scheduler.schedule(this, queueUpdater@{
                    if (!utilsmanager.sqlManager.hasConnection()) return@queueUpdater
                    utilsmanager.sqlManager.runChanges()
                }, 1, 1, TimeUnit.SECONDS)
            }
        }, 1, TimeUnit.SECONDS)
    }

    override fun onDisable() {
        log("§eUnloading systems...")
        BungeeAPI.controller.unregister() // EduardAPI
        RedisBungeeAPI.proxyServerTask?.cancel()
        RedisAPI.finishConnection()
        mySqlQueueUpdater?.cancel()
        utilsmanager.dbManager.closeConnection()
        log("§cPlugin unloaded!"); MKPluginSystem.loadedMKPlugins.remove(this@UtilsBungeeMain)
    }

    private fun prepareRedis() {
        RedisAPI.managerData = config["Redis", RedisConnectionData::class.java]
        if (RedisAPI.managerData.isEnabled) {
            log("§eConnecting to Redis server...")
            RedisAPI.createClient(RedisAPI.managerData)
            RedisAPI.connectClient()
            if (!RedisAPI.isInitialized()) error("Cannot connect to Redis server")
            RedisAPI.useToSyncBungeePlayers = RedisAPI.managerData.syncBungeeDataUsingRedis
            if (RedisAPI.useToSyncBungeePlayers) {
                RedisBungeeAPI.proxyServerOnEnable()
            }
            log("§aConnected to Redis server!")
        } else {
            log("§cRedis is not active on the config file. Some plugins and MK systems may not work correctly.")
        }
    }

    private fun prepareMySQL() {
        manager.sqlManager = SQLManager(config["Database", DBManager::class.java])
        if (manager.sqlManager.dbManager.isEnabled) {
            log("§eConnecting to MySQL database...")
            utilsmanager.dbManager.openConnection()
            if (!utilsmanager.sqlManager.hasConnection()) error("Cannot connect to MySQL database")
            log("§aConnected to MySQL database!")
        } else {
            log("§cThe MySQL is not active on the config file. Some plugins and MK systems may not work correctly.")
        }
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
        config.saveConfig()
    }

    private fun prepareBasics() {
        DBManager.setDebug(false) // EduardAPI
        Config.isDebug = false // EduardAPI
        Copyable.setDebug(false) // EduardAPI
        Command.MESSAGE_PERMISSION = "§cYou don't have permission to use this command." // EduardAPI
    }

    override val isFree: Boolean get() = true

    override fun log(msg: String) {
        ProxyServer.getInstance().console.sendMessage("§b[${systemName}] §f${msg}".toTextComponent())
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