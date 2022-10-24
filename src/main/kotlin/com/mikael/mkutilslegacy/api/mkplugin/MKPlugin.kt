package com.mikael.mkutilslegacy.api.mkplugin

import com.mikael.mkutilslegacy.api.mkplugin.language.LangSystem
import com.mikael.mkutilslegacy.api.mkplugin.language.Translation
import net.eduard.api.lib.plugin.IPluginInstance
import java.util.*

interface MKPlugin : IPluginInstance {

    /**
     * If this [MKPlugin] is free.
     * If it's not, then [MKPluginSystem.requireActivation] will be called to this plugin.
     *
     * @see MKPluginSystem
     */
    val isFree: Boolean

    /**
     * Use to log plugin messages to console.
     *
     * Example: {
     *  override fun log(msg: String) {
     *   Bukkit.getConsoleSender().sendMessage("§b[${systemName}] §f${msg}")
     *   }
     * }
     *
     * @param msg the message to log.
     */
    fun log(msg: String)

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
     * ?
     */
    var regionFormatter: Locale
}