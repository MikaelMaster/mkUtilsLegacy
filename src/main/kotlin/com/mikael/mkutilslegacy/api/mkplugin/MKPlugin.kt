package com.mikael.mkutilslegacy.api.mkplugin

import com.mikael.mkutilslegacy.api.mkplugin.language.LangSystem
import com.mikael.mkutilslegacy.api.mkplugin.language.Translation
import net.eduard.api.lib.plugin.IPluginInstance
import java.util.*

/**
 * Represents a MK Plugin. Extends an [IPluginInstance].
 *
 * @author Mikael
 */
interface MKPlugin : IPluginInstance {

    /**
     * If this [MKPlugin] is free.
     *
     * @author Mikael
     * @see MKPluginSystem
     */
    @Deprecated("Deprecated since mkUtilsLegacy 2.0.6; This is not used anymore for nothing.")
    val isFree: Boolean

    /**
     * The current 'selected' language.
     *
     * Example: 'pt-br', 'en-us'.
     *
     * This will be used the get translations from [Translation].
     * @author Mikael
     * @see LangSystem
     */
    var usingLanguage: String

    /**
     * @author Mikael
     * @see Locale
     */
    var regionFormatter: Locale

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
     * @author Mikael
     */
    fun log(msg: String)

}