package com.mikael.mkutilslegacy.spigot.api.event.menu

import com.mikael.mkutilslegacy.spigot.api.lib.menu.MenuPage
import com.mikael.mkutilslegacy.spigot.api.lib.menu.MineMenu
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after a [MineMenu] is open to a player.
 *
 * This event cannot be cancelled, but you can call [Player.closeInventory] after this event
 * is fired, then the open menu will close.
 */
@Suppress("WARNINGS")
class MineMenuOpenEvent(
    val player: Player,
    /**
     * The open [MineMenu].
     */
    val menu: MineMenu,
    /**
     * The open [MenuPage] of the [menu] for the [player].
     */
    val page: MenuPage
) : Event() {
    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        private fun getHandlerList(): HandlerList = HANDLERS
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

}