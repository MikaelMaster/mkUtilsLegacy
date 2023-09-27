package com.mikael.mkutilslegacy.spigot.api.npc.listener

import com.mikael.mkutilslegacy.api.lib.MineCooldown
import com.mikael.mkutilslegacy.spigot.api.chunk
import com.mikael.mkutilslegacy.spigot.api.lib.MineListener
import com.mikael.mkutilslegacy.spigot.api.npc.PlayerNPC
import com.mikael.mkutilslegacy.spigot.api.npc.PlayerNPCAPI
import com.mikael.mkutilslegacy.spigot.api.runBlock
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.player.*
import org.bukkit.event.world.ChunkUnloadEvent
import kotlin.math.abs
import kotlin.math.atan2

class NPCSystemListener : MineListener() {
    companion object {
        lateinit var instance: NPCSystemListener
    }

    init {
        instance = this@NPCSystemListener
    }

    private val seeingInDistance = mutableMapOf<Player, MutableSet<PlayerNPC>>()
    private val seeingInFov = mutableMapOf<Player, MutableSet<PlayerNPC>>()

    private val fovToCalc = 70.0 // Fov padrão do Minecraft
    private fun flushNPCs(from: Location, to: Location, player: Player) {
        val seeingNpcs = seeingInDistance.getOrPut(player) { mutableSetOf() }
        val seeingInFovNpcs = seeingInFov.getOrPut(player) { mutableSetOf() }
        PlayerNPCAPI.npcs.values.forEach { npc ->
            if (!npc.holders.contains(player)) {
                seeingNpcs.remove(npc)
                seeingInFovNpcs.remove(npc)
                return@forEach
            }
            if (npc.getPlayer().world.name != to.world.name) {
                npc.hideFor(player)
                seeingNpcs.remove(npc)
                seeingInFovNpcs.remove(npc)
                return@forEach
            }

            val distance = to.distance(npc.getPlayer().location)
            if (distance < 45 &&
                !seeingNpcs.contains(npc)
            ) {
                npc.showFor(player, onlyFlush = false)
                seeingNpcs.add(npc)
                // seeingInFovNpcs.add(npc) // Nope!
                return@forEach
            } else if (distance >= 45) {
                seeingNpcs.remove(npc)
                seeingInFovNpcs.remove(npc)
                return@forEach
            }

            val npcLoc = npc.getSpawnLocation()
            val playerDirection = from.direction.normalize()
            val entityDirection = npcLoc.toVector().subtract(to.toVector()).normalize()
            var angle = Math.toDegrees(
                atan2(entityDirection.x, entityDirection.z) - atan2(
                    playerDirection.x,
                    playerDirection.z
                )
            )
            if (angle > 180) {
                angle -= 360
            } else if (angle < -180) {
                angle += 360
            }
            val isToFlush = abs(angle) <= fovToCalc
            if (isToFlush) {
                seeingInFovNpcs.add(npc)
                return@forEach // Ele já está no campo de visão, ent fds
            }

            val npcLoc2 = npc.getSpawnLocation()
            val playerDirection2 = to.direction.normalize()
            val entityDirection2 = npcLoc2.toVector().subtract(to.toVector()).normalize()
            var angle2 = Math.toDegrees(
                atan2(entityDirection2.x, entityDirection2.z) - atan2(
                    playerDirection2.x,
                    playerDirection2.z
                )
            )
            if (angle2 > 180) {
                angle2 -= 360
            } else if (angle2 < -180) {
                angle2 += 360
            }
            val isToFlush2 = abs(angle2) <= fovToCalc
            if (isToFlush2 && !seeingInFovNpcs.contains(npc)) { // Fds o isToFlush, já da return (continue) lá em cima
                npc.showFor(player, onlyFlush = true)
                seeingNpcs.add(npc)
                seeingInFovNpcs.add(npc)
            }
        }
        seeingInDistance[player] = seeingNpcs
        seeingInFov[player] = seeingInFovNpcs
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        PlayerNPCAPI.npcs.values.forEach { npc ->
            if (!npc.shouldSpawnForNewPlayers) return@forEach
            npc.holders.add(player)
            flushNPCs(player.location, player.location, player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(e: PlayerQuitEvent) {
        val player = e.player
        player.runBlock {
            PlayerNPCAPI.npcs.values.forEach { npc ->
                npc.holders.remove(player)
            }
            seeingInDistance.remove(player)
            seeingInFov.remove(player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        val player = e.player
        player.runBlock {
            val from = e.from
            val to = e.to
            // if (Mine.equals2(from, to)) return@runBlock
            flushNPCs(from, to, player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(e: PlayerMoveEvent) {
        val player = e.player
        player.runBlock {
            val from = e.from
            val to = e.to
            // if (Mine.equals2(from, to)) return@runBlock
            flushNPCs(from, to, player)
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