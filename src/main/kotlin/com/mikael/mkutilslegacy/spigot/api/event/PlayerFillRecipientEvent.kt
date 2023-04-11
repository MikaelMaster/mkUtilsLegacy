package com.mikael.mkutilslegacy.spigot.api.event

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

/**
 * Called when a player fills a recipient ([Material.WATER_BUCKET] or [Material.GLASS_BOTTLE]).
 */
@Suppress("WARNINGS")
class PlayerFillRecipientEvent(
    val player: Player,
    val material: Material,
    val clickedBlock: Block,
    var item: ItemStack
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