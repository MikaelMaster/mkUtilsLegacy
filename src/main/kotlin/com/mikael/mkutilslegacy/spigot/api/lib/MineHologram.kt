package com.mikael.mkutilslegacy.spigot.api.lib

import com.mikael.mkutilslegacy.spigot.api.chunk
import com.mikael.mkutilslegacy.spigot.api.newHologram
import net.eduard.api.lib.abstraction.Hologram
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player

/**
 * [MineHologram] util class
 *
 * This class was created to replace [World.newHologram] (both functions) of 'SpigotExtensions.kt'.
 * With this util class you can also create 'player-holograms'. In other words, create/show holograms just for specific players.
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
open class MineHologram(vararg val lines: String?) {
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
        lastSpawnLoc = loc
        lastWasToDown = toDown
        return this
    }

    /**
     * @return this [MineHologram].
     */
    fun spawn(loc: Location, toDown: Boolean = true): MineHologram {
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
        lastSpawnLoc = loc
        lastWasToDown = toDown
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
            despawn()
            spawn(loc, isToDown)
        } else {
            despawn(player)
            spawn(player, loc, isToDown)
        }
        return this
    }
}