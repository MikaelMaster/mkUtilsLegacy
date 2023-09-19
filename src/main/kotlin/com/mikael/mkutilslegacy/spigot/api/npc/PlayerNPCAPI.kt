package com.mikael.mkutilslegacy.spigot.api.npc

import com.mikael.mkutilslegacy.api.breakLines
import com.mikael.mkutilslegacy.spigot.UtilsMain
import com.mikael.mkutilslegacy.spigot.api.lib.hologram.MineHologram
import com.mikael.mkutilslegacy.spigot.api.npc.enums.NPCTextBalloonState
import com.mikael.mkutilslegacy.spigot.api.npc.listener.NPCSystemListener
import com.mikael.mkutilslegacy.spigot.api.npc.npc_1_8_R3.PlayerNPC_1_8_R3
import com.mikael.mkutilslegacy.spigot.api.soundClick
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.scheduler.BukkitTask
import java.util.*

/**
 * mkUtils [PlayerNPCAPI]
 *
 * This is a simple API to spawn and manage 'Player NPCs' in a spigot/paper server.
 * This API is fully with NMS, and it's compatible ONLY with minecraft server v*1_8_R3*.
 *
 * Credits for *Eduard*, author of EduardAPI for the searching to do this API.
 *
 * @author Eduard
 * @author Mikael
 * @see PlayerNPC
 */
@Suppress("WARNINGS")
object PlayerNPCAPI {

    /**
     * A map with created [PlayerNPC]s.
     */
    val npcs = mutableMapOf<UUID, PlayerNPC>()

    /**
     * Creates a new NPC.
     * After creating an NPC, you should call for the returned NPC *[PlayerNPC.spawn]*.
     */
    fun create(
        world: World,
        name: String = "§8[NPC]",
        spawnForNewPlayers: Boolean = true,
        lookNearbyPlayers: Boolean = false
    ): PlayerNPC {
        val playerNPC = PlayerNPC_1_8_R3(name, world)
        playerNPC.shouldSpawnForNewPlayers = spawnForNewPlayers
        playerNPC.shouldLookNearbyPlayers = lookNearbyPlayers
        npcs[playerNPC.getId()] = playerNPC
        return playerNPC
    }

    /**
     * @return True if the given [player] is an NPC created by [PlayerNPCAPI]. Otherwise, false.
     */
    fun isNPC(player: Player): Boolean {
        return npcs.containsKey(player.uniqueId)
    }

    /**
     * Despawns the given [npc].
     *
     * @see PlayerNPC.despawn
     */
    fun despawn(npc: PlayerNPC) {
        headExtraHolos[npc]?.despawn()
        headExtraHolos.remove(npc)
        npc.despawn()
        npcs.remove(npc.getId())
    }

    // Extra Section - Start

    internal val nickHiders = mutableMapOf<PlayerNPC, ArmorStand>()

    /**
     * Hides the nick of the given [npc] using a Minecraft client 'glitch'.
     */
    fun hideHeadNick(npc: PlayerNPC) {
        // if (!npc.isSpawned()) return false
        val npcPlayer = npc.getPlayer()
        if (!npcPlayer.location.chunk.isLoaded) {
            npcPlayer.location.chunk.load(true)
        }
        val nickHider = npcPlayer.world.spawn(npcPlayer.location, ArmorStand::class.java)
        nickHider.isCustomNameVisible = false
        nickHider.isVisible = false
        nickHider.setGravity(false)
        npcPlayer.passenger = nickHider
        nickHiders[npc] = nickHider
    }

    internal val headExtraHolos = mutableMapOf<PlayerNPC, MineHologram>()

    fun setExtraHeadHolos(npc: PlayerNPC, holo: MineHologram): MineHologram {
        headExtraHolos[npc]?.despawn()
        val loc = npc.getSpawnLocation().clone()
        loc.y += 0.5
        for (line in holo.lines) {
            loc.y += 0.3
        }
        holo.spawn(loc, true)
        headExtraHolos[npc] = holo
        return holo
    }

    fun removeExtraHeadHolos(npc: PlayerNPC) {
        headExtraHolos[npc]?.despawn()
        headExtraHolos.remove(npc)
    }

    fun setNPCClickAction(npc: PlayerNPC, click: (PlayerInteractEntityEvent) -> Unit) {
        NPCSystemListener.instance.npcsClick[npc] = click
    }

    fun removeClickAction(npc: PlayerNPC) {
        NPCSystemListener.instance.npcsClick.remove(npc)
    }

    fun showTextBalloon(
        npc: PlayerNPC,
        player: Player,
        text: String,
        textColorPrefix: String = "§7",
        whenDoneRemoveTicks: Long = 20 * 5,
        callback: (state: NPCTextBalloonState) -> Unit
    ): MineHologram {
        val loc = npc.getSpawnLocation().clone()
        loc.y += 1.5
        val textLines = text.breakLines(40)
        for (line in textLines) {
            loc.y += 0.3
        }
        headExtraHolos[npc]?.let { extraHolo ->
            extraHolo.lines.forEach { _ ->
                loc.y += 0.3
            }
        }
        val holo = MineHologram()
        UtilsMain.instance.asyncTask {
            UtilsMain.instance.syncTask { callback.invoke(NPCTextBalloonState.STARTED) }
            for ((lineIndex, line) in textLines.withIndex()) {
                val holoLines = holo.lines.toMutableList()
                char@ for ((charIndex, char) in line.withIndex()) {
                    val indexLine = holoLines.getOrElse(lineIndex) { holoLines.add(textColorPrefix); textColorPrefix }
                    holoLines[lineIndex] = (indexLine + char)
                    holo.lines = holoLines.toTypedArray()
                    if (lineIndex == 0 && charIndex == 0) {
                        holo.spawn(player, loc, true)
                    } else {
                        holo.update(player)
                    }
                    player.soundClick(2f, 2f)
                    UtilsMain.instance.syncTask { callback.invoke(NPCTextBalloonState.FLUSH_CHARACTERS) }
                    Thread.sleep(50)
                }
                if (lineIndex == textLines.lastIndex) {
                    UtilsMain.instance.syncTask { callback.invoke(NPCTextBalloonState.FINISHED) }
                    UtilsMain.instance.syncDelay(whenDoneRemoveTicks) {
                        holo.despawn(player)
                        callback.invoke(NPCTextBalloonState.REMOVED)
                    }
                }
            }
        }
        return holo
    }

    // Extra Section - End

    private var lookAtNearbyTask: BukkitTask? = null

    /**
     * Internal; Should be run in [UtilsMain] onEnable.
     */
    internal fun onEnable() {
        onDisable()
        lookAtNearbyTask = UtilsMain.instance.syncTimer(2, 2) {
            for (npc in npcs.values.filter { it.isSpawned() && it.shouldLookNearbyPlayers }) {
                val npcPlayer = npc.getPlayer()
                val nearbyPlayer =
                    npcPlayer.getNearbyEntities(5.0, 5.0, 5.0).filterIsInstance<Player>().firstOrNull { !isNPC(it) }
                        ?: continue
                npc.lookAt(nearbyPlayer.location, npc.holders.toList())
            }
        }
    }

    /**
     * Internal; Should be run in [UtilsMain] onDisable.
     */
    internal fun onDisable() {
        lookAtNearbyTask?.cancel(); lookAtNearbyTask = null
        nickHiders.values.forEach { it.remove() }; nickHiders.clear()
        headExtraHolos.values.forEach { it.despawn() }; headExtraHolos.clear()
    }

}