package com.mikael.mkutilslegacy.spigot.api.lib.menu.button

import com.mikael.mkutilslegacy.spigot.api.lib.menu.MenuPage
import com.mikael.mkutilslegacy.spigot.api.lib.menu.MineMenu
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

/**
 * Represents an animated [MenuPage] button.
 *
 * @param name the name ("id") of this button.
 * @author Mikael
 * @see MineMenu
 * @see MenuPage
 * @See MenuButton
 */
@Suppress("WARNINGS")
open class MenuAnimatedButton(name: String) : MenuButton(name) {

    /**
     * Invokes a new [MenuAnimatedButton] using 'null-name-animated-button' as the param [name].
     */
    constructor() : this("null-name-animated-button")

    internal var changeFrameDelay = 20L
    internal var runAnimationTask: BukkitTask? = null
    var frames = mutableListOf<ItemStack>()

}