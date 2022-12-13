package com.mikael.mkutilslegacy.api

import net.eduard.api.lib.database.DBManager
import net.eduard.api.lib.database.SQLManager
import org.bukkit.plugin.java.JavaPlugin

/**
 * mkUtils manager class.
 *
 * @author Mikael
 */
object UtilsManager {

    /**
     * @return the current mkUtils version.
     */
    lateinit var mkUtilsVersion: String

    /**
     * The mkUtils global [SQLManager].
     *
     * IMPORTANT:
     * This value is only started when the plugin starts [JavaPlugin.onEnable].
     * Before the plugin onEnable this value will be 'null'.
     */
    lateinit var sqlManager: SQLManager

    /**
     * The mkUtils global [DBManager].
     *
     * IMPORTANT:
     * This will only be successfully called if the [sqlManager] is already set.
     */
    val dbManager get() = sqlManager.dbManager

}