package com.mikael.mkutilslegacy.spigot.api.lib

import com.mikael.mkutilslegacy.api.mkplugin.MKPlugin
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * [MineListener] util class
 *
 * This class represents a [Listener].
 *
 * To create a new MineListener, extends it in a Class. As the example below:
 * - class TestListener : MineListener() { *class code* }
 *
 * @author Mikael
 * @see Listener
 */
open class MineListener : Listener {

    // Properties - Start

    /**
     * The [MKPlugin] holder (owner) of this [MineListener].
     */
    private var ownerPlugin: MKPlugin? = null

    /**
     * @return the [MKPlugin]? holding (owner) of this [MineListener]. Can be null
     * if this listener is not registered yet.
     */
    fun getOwnerPlugin(): MKPlugin? {
        return ownerPlugin
    }

    /**
     * Note: If [ownerPlugin] is NOT null, means that this [MineListener] is registered.
     * If it's null, means this listener is not yet registered.
     *
     * @return True if the [ownerPlugin] is not null. Otherwise, false.
     */
    val isRegistered get() = ownerPlugin != null

    // Properties - End

    /**
     * Registers this [MineListener].
     *
     * @param plugin the [MKPlugin] holder (owner) of this listener.
     */
    open fun registerListener(plugin: MKPlugin) {
        unregisterListener()
        Bukkit.getPluginManager().registerEvents(this, plugin.plugin as JavaPlugin)
        this.ownerPlugin = plugin
    }

    /**
     * Unregisters this [MineListener].
     *
     * Note: If this listener isn't registered yet ([isRegistered]) nothing will happen.
     */
    fun unregisterListener() {
        if (!isRegistered) return
        HandlerList.unregisterAll(this)
        this.ownerPlugin = null
    }

}