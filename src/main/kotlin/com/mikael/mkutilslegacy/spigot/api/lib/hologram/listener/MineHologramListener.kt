package com.mikael.mkutilslegacy.spigot.api.lib.hologram.listener

import com.mikael.mkutilslegacy.api.lib.MineCooldown
import com.mikael.mkutilslegacy.spigot.api.chunk
import com.mikael.mkutilslegacy.spigot.api.lib.MineListener
import com.mikael.mkutilslegacy.spigot.api.lib.hologram.MineHologram
import com.mikael.mkutilslegacy.spigot.api.runBlock
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.world.ChunkUnloadEvent

class MineHologramListener : MineListener() {
    companion object {
        lateinit var instance: MineHologramListener
    }

    init {
        instance = this@MineHologramListener
    }

    internal val holograms = mutableMapOf<MineHologram, ((PlayerInteractAtEntityEvent) -> Unit)?>()
    private val clickCooldown = MineCooldown(10).apply { noMessages() }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onClickHologram(e: PlayerInteractAtEntityEvent) {
        val player = e.player
        player.runBlock {
            val clicked = e.rightClicked ?: return@runBlock
            if (clicked !is ArmorStand) return@runBlock
            val clickedMineHolo = holograms.keys.firstOrNull { it.getLines().contains(clicked) } ?: return@runBlock
            val clickAction = holograms[clickedMineHolo]
            if (clickAction != null) {
                if (clickCooldown.cooldown(player.name)) {
                    player.runBlock {
                        clickAction.invoke(e)
                    }
                }
            }
            e.isCancelled = true
        }
    }

    internal val keepChunkLoaded = mutableSetOf<MineHologram>()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onHologramChunkUnload(e: ChunkUnloadEvent) {
        if (keepChunkLoaded.none { it.getLines().any { s -> s.chunk == e.chunk } }) return
        e.isCancelled = true
    }

}