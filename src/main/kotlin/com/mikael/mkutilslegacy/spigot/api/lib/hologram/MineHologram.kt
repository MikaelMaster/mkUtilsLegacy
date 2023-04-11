package com.mikael.mkutilslegacy.spigot.api.lib.hologram

import com.mikael.mkutilslegacy.spigot.api.chunk
import com.mikael.mkutilslegacy.spigot.api.lib.hologram.listener.MineHologramListener
import net.eduard.api.lib.abstraction.Hologram
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent

/**
 * [MineHologram] util class
 *
 * With this util class can also create 'player-holograms' and 'global-holograms'.
 * In other words, create/show holograms just for specific players.
 *
 * The 'player-holograms' functions uses [net.minecraft.server.v1_8_R3]! (EduardAPI [Hologram] NMS API)
 *
 * To create/invoke a new MineHologram you can use:
 * - MineHologram(vararg lines: [String]?) -> MineHologram("Line 1", "Line 2", null, "Line 4") -> 'null' will be an empty line.
 *
 * @author KoddyDev
 * @author Mikael
 * @see ArmorStand
 * @see Hologram
 */
@Suppress("WARNINGS")
open class MineHologram(vararg var lines: String?) {
    private val spawnedLines = mutableListOf<ArmorStand>()
    private val spawnedPlayerLines = mutableMapOf<Player, MutableList<Hologram>>()

    private var lastSpawnLoc: Location? = null
    private var lastWasToDown: Boolean? = null

    /**
     * @return all spawned lines of this hologram (List<[ArmorStand]>). Only lines spawned for ALL players will be returned.
     */
    fun getLines(): List<ArmorStand> {
        return spawnedLines
    }

    /**
     * @return all spawned per-player lines of this hologram (Map<[Player], MutableList<[Hologram]>>). Only PER-PLAYER lines will be returned.
     */
    fun getAllSpawnedPlayerLines(): Map<Player, MutableList<Hologram>> {
        return spawnedPlayerLines
    }

    /**
     * @return this [MineHologram].
     */
    fun spawn(player: Player, loc: Location, toDown: Boolean = true): MineHologram {
        lastSpawnLoc = loc.clone()
        lastWasToDown = toDown
        val holos = mutableListOf<Hologram>()
        var location: Location = loc
        for (line in lines) {
            if (line != null) { // generate an 'empty' line if it's null
                val playerHolo = Hologram.create(line)
                playerHolo?.let {
                    it.location = location
                    it.show(player)
                    holos.add(it)
                }
            }
            location = if (toDown) {
                loc.subtract(0.0, 0.3, 0.0)
            } else {
                loc.add(0.0, 0.3, 0.0)
            }
        }
        spawnedPlayerLines[player] = holos
        return this
    }

    /**
     * @return this [MineHologram].
     */
    fun spawn(loc: Location, toDown: Boolean = true): MineHologram {
        lastSpawnLoc = loc.clone()
        lastWasToDown = toDown
        val holos = mutableListOf<ArmorStand>()
        var location: Location = loc
        for (line in lines) {
            if (!loc.chunk.isLoaded) {
                loc.chunk.load(true)
            }
            val holo = loc.world!!.spawn(location, ArmorStand::class.java)
            holo.setGravity(false)
            holo.isVisible = false
            holo.isSmall = true
            holo.isMarker = false
            if (line != null) {
                holo.isCustomNameVisible = true
                holo.customName = line
            } else {
                holo.isCustomNameVisible = false
                holo.customName = "§r" // empty line
            }
            holos.add(holo)
            location = if (toDown) {
                loc.subtract(0.0, 0.3, 0.0)
            } else {
                loc.add(0.0, 0.3, 0.0)
            }
        }
        spawnedLines.addAll(holos)
        return this
    }

    /**
     * This will remove all spawned lines for the given [player]. (If this hologram was not set per-player nothing will happen)
     *
     * @param player the [Player] to hide this hologram. If the player is NULL, [despawn] (with none as param) will be called.
     * @return this [MineHologram].
     */
    fun despawn(player: Player? = null): MineHologram {
        if (player == null) {
            despawn()
            return this
        }
        spawnedPlayerLines[player]?.forEach { it.hide(player) }
        spawnedPlayerLines.remove(player)
        return this
    }

    /**
     * This will remove all [spawnedLines] of this hologram. (Per-player holograms will not be removed using this function)
     *
     * @return this [MineHologram].
     */
    fun despawn(): MineHologram {
        spawnedLines.forEach {
            if (!it.chunk.isLoaded) {
                it.chunk.load(true)
            }
            it.remove()
        }
        spawnedLines.clear()
        return this
    }

    /**
     * Updates this hologram lines for the given [player], using the given [lines].
     *
     * @param player the [Player]? to update this hologram. If null, the global lines (not per-player) will be updated, to everyone.
     * @return this [MineHologram].
     */
    fun update(player: Player? = null): MineHologram {
        val loc = lastSpawnLoc ?: error("MineHologram Last Spawn Loc was not defined; Can't update this hologram.")
        val isToDown = lastWasToDown ?: error("MineHologram Last IsToDown was not defined; Can't update this hologram.")
        if (player == null) {
            if (spawnedLines.size >= lines.size) {
                if (!loc.chunk.isLoaded) {
                    loc.chunk.load(true)
                }
                val newHolograms = spawnedLines.mapIndexed { index, armorStand ->
                    if (index > lines.size - 1) {
                        armorStand.remove()
                        return@mapIndexed null
                    }
                    armorStand.customName = lines[index]
                    armorStand
                }.filterNotNull()

                spawnedLines.clear()
                spawnedLines.addAll(newHolograms)
            } else {
                despawn()
                spawn(loc, isToDown)
            }
        } else {
            despawn(player)
            spawn(player, loc, isToDown)
        }
        return this
    }

    /**
     * Sets a click action for this [MineHologram].
     * When a player clicks in this hologram, the given [click] will be executed.
     *
     * To cancel this event, use the *PRIORITY.HIGH* in your code, then mkUtils
     * will ignore the click, since mkUtils uses the *PRIORITY.HIGHEST* to listen
     * clicks in holograms.
     *
     * @param click the block code to run when a player clicks this hologram.
     */
    fun setClick(click: (PlayerInteractAtEntityEvent) -> Unit): MineHologram {
        MineHologramListener.instance.hologramsClick[this@MineHologram] = click
        return this
    }

    /**
     * Removes the click action set for this hologram using [setClick].
     * If a click action was not set, nothing will happen.
     */
    fun removeClickAction(): MineHologram {
        MineHologramListener.instance.hologramsClick.remove(this@MineHologram)
        return this
    }

    /**
     * Sets if all lines ([getLines], stands) of this mine hologram should not
     * have their chunks unloaded.
     *
     * @param keepChunkLoaded is it's to keep the chunk loaded or not.
     */
    fun keepHoloChunkLoaded(keepChunkLoaded: Boolean): MineHologram {
        if (keepChunkLoaded) {
            MineHologramListener.instance.keepChunkLoaded.add(this@MineHologram)
        } else {
            MineHologramListener.instance.keepChunkLoaded.remove(this@MineHologram)
        }
        return this
    }
}