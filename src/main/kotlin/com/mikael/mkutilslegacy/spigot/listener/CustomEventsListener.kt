package com.mikael.mkutilslegacy.spigot.listener

import com.mikael.mkutilslegacy.spigot.api.event.PlayerFillRecipientEvent
import com.mikael.mkutilslegacy.spigot.api.event.PlayerThrowPotionEvent
import com.mikael.mkutilslegacy.spigot.api.lib.MineListener
import com.mikael.mkutilslegacy.spigot.api.runBlock
import com.mikael.mkutilslegacy.spigot.api.utilsMain
import net.eduard.api.lib.kotlin.mineCallEvent
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.Potion

class CustomEventsListener : MineListener() {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerFillRecipient(e: PlayerInteractEvent) {
        val player = e.player
        player.runBlock {
            val item = e.item ?: return@runBlock
            if (Material.BUCKET != item.type &&
                Material.GLASS_BOTTLE != item.type
            ) return@runBlock
            val clickedBlock = player.getTargetBlock(setOf(Material.AIR), 5) ?: return@runBlock
            if (Material.WATER != clickedBlock.type &&
                Material.STATIONARY_WATER != clickedBlock.type &&
                Material.LAVA != clickedBlock.type &&
                Material.STATIONARY_LAVA != clickedBlock.type
            ) return@runBlock

            if (Material.LAVA == clickedBlock.type ||
                Material.STATIONARY_LAVA == clickedBlock.type
            ) {
                if (Material.GLASS_BOTTLE == item.type) return@runBlock
            }

            // Calls PlayerFillRecipientEvent
            val playerFillBottleEvent = PlayerFillRecipientEvent(player, item.type, clickedBlock, item.clone())
            playerFillBottleEvent.mineCallEvent() // Calls this event
            if (playerFillBottleEvent.isCancelled) {
                e.setUseItemInHand(Event.Result.DENY)
                e.setUseInteractedBlock(Event.Result.DENY)
                player.itemInHand = item
            }
            e.isCancelled = playerFillBottleEvent.isCancelled
            utilsMain.syncDelay(1) {
                player.updateInventory()
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerThrowPotion(e: PlayerInteractEvent) {
        val player = e.player
        player.runBlock {
            val item = e.item ?: return@runBlock
            if (Material.POTION != item.type) return@runBlock
            if (!Potion.fromItemStack(item).isSplash) return@runBlock

            val playerThrowPotionEvent = PlayerThrowPotionEvent(player, item.clone())
            playerThrowPotionEvent.mineCallEvent() // Calls this event
            if (playerThrowPotionEvent.isCancelled) {
                e.setUseItemInHand(Event.Result.DENY)
                e.setUseInteractedBlock(Event.Result.DENY)
                player.itemInHand = item
            }
            e.isCancelled = playerThrowPotionEvent.isCancelled
            utilsMain.syncDelay(1) {
                player.updateInventory()
            }
        }
    }

}