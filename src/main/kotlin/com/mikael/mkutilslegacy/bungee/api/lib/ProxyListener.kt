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
 * To create a new MineListener, extends it in a Class. As the example below:
 * - class TestListener : ProxyListener() { *class code* }
 *
 * @author Mikael
 * @see Listener
 */
@Suppress("WARNINGS")
open class ProxyListener : Listener {

    // Properties - Start

    /**
     * The [MKPlugin] holder (owner) of this [ProxyListener].
     */
    private var ownerPlugin: MKPlugin? = null

    /**
     * @return the [MKPlugin]? holding (owner) of this [ProxyListener]. Can be null
     * if this listener is not registered yet.
     */
    fun getOwnerPlugin(): MKPlugin? {
        return ownerPlugin
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