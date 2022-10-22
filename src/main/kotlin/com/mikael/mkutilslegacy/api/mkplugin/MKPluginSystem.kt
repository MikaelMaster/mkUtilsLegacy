package com.mikael.mkutilslegacy.api.mkplugin

object MKPluginSystem {

    val loadedMKPlugins = mutableListOf<MKPlugin>()

    init {
        loadedMKPlugins.clear()
    }

    /**
     * MK Store section.
     * This will be only used for paid proprietary MK plugins.
     *
     * @author Mikael
     * @see MKPlugin
     */
    const val storeVersion = "v1.0b"

    /**
     * @param plugin the paid [MKPlugin] to check license.
     * @param customerToken the Customer Token to check if he has the access to the given [plugin].
     * @return True if the license is valid. Otherwise, false.
     * @throws IllegalStateException if the given [plugin] is not a paid [MKPlugin]. In other worlds, it's a [MKPlugin.isFree] (true) plugin.
     */
    fun requireActivation(plugin: MKPlugin, customerToken: String): Boolean {
        return try {
            plugin.log("§6[License] §eChecking Customer Token...")
            if (plugin.isFree) error("Cannot verify the license (token) of a free MK Plugin")
            Thread.sleep(1000) // code here
            plugin.log("§6[License] §aThe Customer Token is valid. Starting load...")
            true
        } catch (ex: Exception) {
            plugin.log("§6[License] §cThe Customer Token is not valid. Shutting down...")
            ex.printStackTrace()
            false
        }
    }
}