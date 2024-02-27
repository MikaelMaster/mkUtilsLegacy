package com.mikael.mkutilslegacy.api.mkplugin

/**
 * @see MKPlugin
 */
@Suppress("WARNINGS")
object MKPluginSystem {

    internal val loadedMKPlugins = mutableSetOf<MKPlugin>()

    init {
        loadedMKPlugins.clear()
    }

    /**
     * @return all loaded [MKPlugin]s.
     * @see loadedMKPlugins
     */
    fun getLoadedMKPlugins(): List<MKPlugin> {
        return loadedMKPlugins.toList()
    }

    /**
     * Registers the given [plugin] into [loadedMKPlugins].
     * So mkUtilsLegacy can property manage the [MKPlugin] systems.
     *
     * Should be used on the end of the [plugin] onEnable.
     *
     * @param plugin the [MKPlugin] to register.
     */
    fun registerMKPlugin(plugin: MKPlugin) {
        loadedMKPlugins.add(plugin)
    }

    /**
     * Unregisters the given [plugin].
     *
     * Should be used on the end of the [plugin] onDisable.
     *
     * @param plugin the [MKPlugin] to register.
     */
    fun unregisterMKPlugin(plugin: MKPlugin) {
        loadedMKPlugins.remove(plugin)
    }

}