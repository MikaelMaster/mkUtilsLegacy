package com.mikael.mkutilslegacy.spigot.api.npc.listener

import com.mikael.mkutilslegacy.api.lib.MineCooldown
import com.mikael.mkutilslegacy.spigot.api.chunk
import com.mikael.mkutilslegacy.spigot.api.lib.MineListener
import com.mikael.mkutilslegacy.spigot.api.npc.PlayerNPC
import com.mikael.mkutilslegacy.spigot.api.npc.PlayerNPCAPI
import com.mikael.mkutilslegacy.spigot.api.runBlock
import net.eduard.api.lib.modules.Mine
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.player.*
import org.bukkit.event.world.ChunkUnloadEvent

class NPCSystemListener : MineListener() {
    companion object {
        lateinit var instance: NPCSystemListener
    }

    init {
        instance = this@NPCSystemListener
    }

    private val seeing = mutableMapOf<Player, MutableSet<PlayerNPC>>()

    private fun flushNPCs(toLoc: Location, player: Player) {
        val seeingNpcs = seeing.getOrPut(player) { mutableSetOf() }
        PlayerNPCAPI.npcs.values.forEach { npc ->
            if (!npc.holders.contains(player)) {
                seeingNpcs.remove(npc)
                return@forEach
            }
            if (npc.getPlayer().world.name != toLoc.world.name) {
                npc.hideFor(player)
                seeingNpcs.remove(npc)
                return@forEach
            }

            val distance = toLoc.distance(npc.getPlayer().location)
            if (distance < 45 &&
                !seeingNpcs.contains(npc)
            ) {
                npc.showFor(player)
                seeingNpcs.add(npc)
            } else if (distance >= 45) {
                seeingNpcs.remove(npc)
            }
        }
        seeing[player] = seeingNpcs
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        PlayerNPCAPI.npcs.values.forEach { npc ->
            if (!npc.shouldSpawnForNewPlayers) return@forEach
            npc.holders.add(player)
            flushNPCs(player.location, player)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onQuit(e: PlayerQuitEvent) {
        val player = e.player
        player.runBlock {
            PlayerNPCAPI.npcs.values.forEach { npc ->
                npc.holders.remove(player)
            }
            seeing.remove(player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        val player = e.player
        player.runBlock {
            if (Mine.equals2(e.from, e.to)) return@runBlock
            flushNPCs(e.to, player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(e: PlayerMoveEvent) {
        val player = e.player
        player.runBlock {
            if (Mine.equals2(e.from, e.to)) return@runBlock
            flushNPCs(e.to, player)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun noDamageNPC(e: EntityDamageByEntityEvent) {
        if (e.entity !is Player) return
        val player = e.entity as Player
        if (!PlayerNPCAPI.isNPC(player)) return
        e.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onTarget(e: EntityTargetEvent) {
        // Make non-peaceful mobs ignore NPCs and don't try to hit them
        if (e.target !is Player) return
        val target = e.target as Player
        if (!PlayerNPCAPI.isNPC(target)) return
        e.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChunkUnload(e: ChunkUnloadEvent) {
        if (PlayerNPCAPI.nickHiders.none { it.value.chunk == e.chunk } &&
            PlayerNPCAPI.headExtraHolos.values.none { it.getLines().any { line -> line.chunk == e.chunk } }
        ) return
        e.isCancelled = true
    }

    // Extra Section - Start

    internal val npcsClick = mutableMapOf<PlayerNPC, ((PlayerInteractEntityEvent) -> Unit)>()
    private val clickCooldown = MineCooldown(10).apply { noMessages() }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onClickNPC(e: PlayerInteractEntityEvent) {
        val player = e.player
        player.runBlock {
            val clicked = e.rightClicked ?: return@runBlock
            if (clicked !is Player) return@runBlock // Player here is equivalent to a PlayerNPC
            val npc = PlayerNPCAPI.npcs[clicked.uniqueId] ?: return@runBlock
            val click = npcsClick[npc] ?: return@runBlock
            if (clickCooldown.cooldown(player.name)) {
                player.runBlock {
                    click.invoke(e)
                }
            }
            e.isCancelled = true
        }
    }

    // Extra Section - End

}