package com.mikael.mkutilslegacy.bungee

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
import com.mikael.mkutilslegacy.api.toTextComponent
import com.mikael.mkutilslegacy.bungee.api.utilsBungeeMain
import com.mikael.mkutilslegacy.bungee.command.BungeeVersionCommand
import net.eduard.api.lib.bungee.BungeeAPI
import net.eduard.api.lib.command.Command
import net.eduard.api.lib.config.Config
import net.eduard.api.lib.database.DBManager
import net.eduard.api.lib.database.HybridTypes
import net.eduard.api.lib.hybrid.BungeeServer
import net.eduard.api.lib.hybrid.Hybrid
import net.eduard.api.lib.kotlin.store
import net.eduard.api.lib.modules.Copyable
import net.eduard.api.lib.storage.StorageAPI
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * mkUtils (non-legacy for bungee/proxy) plugin main Bungee/Proxy class.
 *
 * Special thanks to [Eduard](https://github.com/EduardMaster).
 * This project uses some [EduardAPI/MineToolkit](https://github.com/EduardMaster/MineToolkit) extras and methods.
 *
 * @author Mikael
 * @author KoddyDev
 * @author Eduard (EduardAPI/MineToolkit)
 *
 * @see MKPlugin
 * @see utilsBungeeMain
 */
class UtilsBungeeMain : Plugin(), MKPlugin {
    companion object {
        lateinit var instance: UtilsBungeeMain
    }

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
        prepareDatabaseAPI(); prepareRedisAPI()

        // Commands
        BungeeVersionCommand().registerCommand(this)

        val loadTime = System.currentTimeMillis() - loadStart
        log(LangSystem.getText(Translation.LOADING_COMPLETE).replace("%time_taken%", "$loadTime"))
        MKPluginSystem.registerMKPlugin(this@UtilsBungeeMain)

        // Log loaded MK Plugins after proxy server is 'done loading'
        ProxyServer.getInstance().scheduler.schedule(this, delay@{
            log("§aLoaded MK Plugins:")
            for (mkPlugin in MKPluginSystem.getLoadedMKPlugins()) {
                val mkProxyPl = mkPlugin.plugin as Plugin
                log(" §7▪ §e${mkProxyPl.description.name} v${mkProxyPl.description.version}")
            }
        }, 1, TimeUnit.SECONDS)
    }

    override fun onDisable() {
        log(LangSystem.getText(Translation.UNLOADING_SYSTEMS))

        BungeeAPI.controller.unregister() // EduardAPI
        RedisBungeeAPI.Bungee.onDisableStopRedisSub()
        RedisAPI.unloadRedisAPI()
        DatabaseAPI.unloadDatabaseAPI()

        log(LangSystem.getText(Translation.UNLOADING_COMPLETE))
        MKPluginSystem.unregisterMKPlugin(this@UtilsBungeeMain)
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
            RedisBungeeAPI.Bungee.onEnableStartRedisPubSub()
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
        StorageAPI.startGson() // EduardAPI
    }

    private fun reloadConfigs() {
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
            "RedisBungeeAPI.logSpigotServersPowerActions",
            false,
            "Log the power actions of Spigot servers received from RedisBungeeAPI into the Proxy console."
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
            "Don't change it if you don't have sure what you're doing!"
        )
        config.saveConfig()
    }

    private fun prepareBasics() {
        DBManager.setDebug(false) // EduardAPI
        Config.isDebug = false // EduardAPI
        Copyable.setDebug(false) // EduardAPI
        Command.MESSAGE_PERMISSION = "§cYou don't have permission to use this command." // EduardAPI
    }

    override var usingLanguage: String = "en-us"
    override var regionFormatter: Locale = Locale.US

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