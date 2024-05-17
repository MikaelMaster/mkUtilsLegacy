package com.mikael.mkutilslegacy.bungee.api.lib

import com.mikael.mkutilslegacy.api.mkplugin.MKPlugin
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin

/**
 * [ProxyListener] util class
 *
 * This class represents a [Listener].
 *
 * To create a new ProxyListener, extends it in a Class. As the example below:
 * ```
 * class TestListener : ProxyListener() {}
 * ```
 *
 * @author Mikael
 * @see Listener
 */
@Suppress("WARNINGS")
open class ProxyListener : Listener {

    // Properties - Start
    private var _ownerPlugin: MKPlugin? = null

    /**
     * The [MKPlugin] holder (owner) of this [ProxyListener].
     */
    var ownerPlugin: MKPlugin?
        get() = _ownerPlugin
        private set(value) {
            _ownerPlugin = value
        }

    /**
     * Note: If [ownerPlugin] is NOT null, means that this [ProxyListener] is registered.
     * If it's null, means this listener is not yet registered.
     *
     * @return True if the [ownerPlugin] is not null. Otherwise, false.
     */
    val isRegistered get() = ownerPlugin != null
    // Properties - End

    /**
     * Registers this [ProxyListener].
     *
     * @param plugin the [MKPlugin] holder (owner) of this listener.
     */
    open fun registerListener(plugin: MKPlugin) {
        unregisterListener()
        ProxyServer.getInstance().pluginManager.registerListener((plugin.plugin as Plugin), this)
        this.ownerPlugin = plugin
    }

    /**
     * Unregisters this [ProxyListener].
     *
     * Note: If this listener isn't registered yet ([isRegistered]) nothing will happen.
     */
    fun unregisterListener() {
        if (!isRegistered) return
        ProxyServer.getInstance().pluginManager.unregisterListener(this)
        this.ownerPlugin = null
    }

}