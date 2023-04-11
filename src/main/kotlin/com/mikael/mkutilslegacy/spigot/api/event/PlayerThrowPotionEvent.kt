package com.mikael.mkutilslegacy.spigot.api.event

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.inventory.ItemStack

/**
 * Called when a player throws a splash potion.
 * This is called BEFORE [PotionSplashEvent].
 */
@Suppress("WARNINGS")
class PlayerThrowPotionEvent(
    val player: Player,
    /**
     * The thrown potion [ItemStack].
     * If this is changed, the item in the hand of the [player] will be changed too.
     */
    val item: ItemStack,
) : Event(), Cancellable {
    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        private fun getHandlerList(): HandlerList = HANDLERS
    }

    private var cancelled = false

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(p0: Boolean) {
        cancelled = p0
    }
}