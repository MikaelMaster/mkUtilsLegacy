package com.mikael.mkutilslegacy.api.mkplugin

/**
 * @see MKPlugin
 */
object MKPluginSystem {

    val loadedMKPlugins = mutableListOf<MKPlugin>()

    init {
        loadedMKPlugins.clear()
    }

}