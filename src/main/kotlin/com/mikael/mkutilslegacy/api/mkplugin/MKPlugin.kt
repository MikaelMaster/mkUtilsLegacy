package com.mikael.mkutilslegacy.api.mkplugin

import com.mikael.mkutilslegacy.api.mkplugin.language.LangSystem
import com.mikael.mkutilslegacy.api.mkplugin.language.Translation
import net.eduard.api.lib.plugin.IPluginInstance
import java.util.*

/**
 * Represents an MK Plugin. Extends an [IPluginInstance].
 *
 * @author Mikael
 */
interface MKPlugin : IPluginInstance {

    /**
     * The current 'selected' language.
     *
     * Example: 'pt-br', 'en-us'.
     *
     * This will be used the get translations from [Translation].
     * @see LangSystem
     */
    var usingLanguage: String

    /**
     * @see Locale
     */
    var regionFormatter: Locale

    /**
     * Use to log plugin messages to console.
     *
     *
     * Example for Spigot:
     * ```
     *  override fun log(vararg msg: String) {
     *      Bukkit.getConsoleSender().sendMessage(msg)
     *  }
     *```
     *
     * Example for Bungee:
     * ```
     *  override fun log(vararg msg: String) {
     *      ProxyServer.getInstance().console.sendMessage(*msg.map { it.toTextComponent() }.toTypedArray())
     *  }
     *  ```
     *
     * @param msgs the messages to log.
     */
    fun log(vararg msgs: String)

}