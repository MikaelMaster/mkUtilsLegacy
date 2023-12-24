package com.mikael.mkutilslegacy.spigot.listener

import com.mikael.mkutilslegacy.spigot.api.event.PlayerChangeOpenableEvent
import com.mikael.mkutilslegacy.spigot.api.event.PlayerFillRecipientEvent
import com.mikael.mkutilslegacy.spigot.api.event.PlayerThrowPotionEvent
import com.mikael.mkutilslegacy.spigot.api.lib.MineListener
import com.mikael.mkutilslegacy.spigot.api.runBlock
import com.mikael.mkutilslegacy.spigot.api.utilsMain
import net.eduard.api.lib.kotlin.mineCallEvent
import net.minecraft.server.v1_8_R3.BlockPosition
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
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

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerOpenOrCloseOpenable(e: PlayerInteractEvent) {
        val player = e.player
        player.runBlock {
            val block = e.clickedBlock ?: return@runBlock
            val isOpen = when {
                isDoor(block.location) -> isDoorOpen(block.location)
                isTrapdoor(block.location) -> isTrapdoorOpen(block.location)
                else -> return@runBlock
            }
            // !isOpen because the PlayerInteractEvent is called BEFORE the change is made and
            // the PlayerChangeOpenableEvent requires a toSate, so the state after the change is made.
            val state = if (!isOpen) PlayerChangeOpenableEvent.OpenableState.OPEN
            else PlayerChangeOpenableEvent.OpenableState.CLOSED
            val playerChangeOpenableEvent = PlayerChangeOpenableEvent(player, block, state)
            playerChangeOpenableEvent.mineCallEvent() // Calls this event
            e.isCancelled = playerChangeOpenableEvent.isCancelled
        }
    }

    private fun isDoor(location: Location): Boolean {
        val nmsWorld = (location.world as CraftWorld).handle
        val blockPosition = BlockPosition(location.blockX, location.blockY, location.blockZ)
        val blockData = nmsWorld.getType(blockPosition)
        return (blockData.block.material.isBuildable && blockData.block is net.minecraft.server.v1_8_R3.BlockDoor)
    }

    private fun isDoorOpen(location: Location): Boolean {
        val nmsWorld = (location.world as CraftWorld).handle
        val blockPosition = BlockPosition(location.blockX, location.blockY, location.blockZ)
        val blockData = nmsWorld.getType(blockPosition)
        return (blockData.block is net.minecraft.server.v1_8_R3.BlockDoor && (blockData.get(net.minecraft.server.v1_8_R3.BlockDoor.OPEN) || blockData.get(
            net.minecraft.server.v1_8_R3.BlockDoor.HALF
        ) == net.minecraft.server.v1_8_R3.BlockDoor.EnumDoorHalf.UPPER))
    }

    private fun isTrapdoor(location: Location): Boolean {
        val nmsWorld = (location.world as CraftWorld).handle
        val blockPosition = BlockPosition(location.blockX, location.blockY, location.blockZ)
        val blockData = nmsWorld.getType(blockPosition)
        return (blockData.block.material.isBuildable && blockData.block is net.minecraft.server.v1_8_R3.BlockTrapdoor)
    }

    private fun isTrapdoorOpen(location: Location): Boolean {
        val nmsWorld = (location.world as CraftWorld).handle
        val blockPosition = BlockPosition(location.blockX, location.blockY, location.blockZ)
        val blockData = nmsWorld.getType(blockPosition)
        return (blockData.block is net.minecraft.server.v1_8_R3.BlockTrapdoor && blockData.get(net.minecraft.server.v1_8_R3.BlockTrapdoor.OPEN))
    }

}