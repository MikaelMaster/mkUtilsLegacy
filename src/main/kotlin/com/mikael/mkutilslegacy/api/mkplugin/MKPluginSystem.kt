package com.mikael.mkutilslegacy.api.mkplugin

/**
 * @see MKPlugin
 */
object MKPluginSystem {

    internal val loadedMKPlugins = mutableListOf<MKPlugin>()

    init {
        loadedMKPlugins.clear()
    }

    fun registerMKPlugin(plugin: MKPlugin) {
        loadedMKPlugins.add(plugin)
    }

    fun unregisterMKPlugin(plugin: MKPlugin) {
        loadedMKPlugins.remove(plugin)
    }

}