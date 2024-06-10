@file:Suppress("WARNINGS")

package com.mikael.mkutilslegacy.api.mkplugin

import com.mikael.mkutilslegacy.api.mkplugin.language.LangSystem
import com.mikael.mkutilslegacy.api.mkplugin.language.Translation
import net.eduard.api.lib.plugin.IPluginInstance
import java.util.*

/**
 * Represents an MK Plugin.
 * Extends an [IPluginInstance] from EduardAPI to expand compability.
 *
 * @author Mikael
 * @see IPluginInstance
 * @see MKPluginSystem
 */
interface MKPlugin: IPluginInstance {

    /**
     * The current 'selected' language.
     *
     * Example: 'pt-br', 'en-us'.
     *
     * This will be used the get translations from [Translation].
     * @see LangSystem
     */
    var usingLanguage: String
        get() = "en-us"
        set(value) = TODO()

    /**
     * @see Locale
     */
    var regionFormatter: Locale
        get() = Locale.US
        set(value) = TODO("Do code here")

    /**
     * Use to log plugin messages to console.
     *
     *
     * Example for Spigot:
     * ```
     *  override fun log(vararg msg: String) {
     *      msg.forEach {
     *             Bukkit.getConsoleSender().sendMessage("§b[${systemName}] §f${it}")
     *         }
     *  }
     *```
     *
     * Example for Bungee:
     * ```
     *  override fun log(vararg msg: String) {
     *      msg.forEach {
     *             ProxyServer.getInstance().console.sendMessage("§b[${systemName}] §f${it}".toTextComponent())
     *         }
     *  }
     *  ```
     *
     * @param msg the messages to log.
     */
    fun log(vararg msg: String)

}