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
 * The 'player-holograms' functions uses [net.minecraft.server.v1_8_R3]! (NMS 1.8_R3)
 *
 * To create/invoke a new MineHologram you can use:
 * - MineHologram(vararg lines: [String]?) -> MineHologram("Line 1", "Line 2", null, "Line 4") -> 'null' will be an empty line.
 *
 * @author KoddyDev
 * @author Mikael
 * @see ArmorStand
 */
open class MineHologram(private vararg val lines: String?) {
    private val spawnedLines = mutableListOf<ArmorStand>()
    private val spawnedPlayerLines = mutableMapOf<Player, MutableList<Hologram>>()

    /**
     * @return all spawned lines of this hologram (List<[ArmorStand]>). Only lines spawned for ALL players will be returned.
     */
    fun getLines(): List<ArmorStand> {
        return spawnedLines
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
        return this
    }

    /**
     *
     * @param player the [Player] to hide (despawn) this hologram. If the player is NULL, [despawn] will be called.
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
     * This will remove (despawn) all [spawnedLines].
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

    /*
    fun spawn(): MutableList<ArmorStand> {
        remove()
        val list = Mine.newHologram(location, lines.toMutableList())

        holos.addAll(list)
        return list
    }

    /**
     * Update a line of a Hologram
     *
     * @author KoddyDev
     * @param line to update
     * @param text to update
     * @return [Boolean] if updated
     */
    fun update(line: Int, text: String): Boolean {
        if (line >= holos.size) return false
        holos[line].customName = text

        return true
    }

    /**
     * Update all lines of a Hologram
     *
     * @author KoddyDev
     * @param lines Lines to update
     * @return [Boolean] if updated
     */
    fun update(vararg lines: String): Boolean {
        if (lines.size != holos.size) {
            Mine.newHologram(location, lines.toMutableList())
        } else {
            for (i in lines.indices) {
                holos[i].customName = lines[i]
            }
        }

        return true
    }

    /**
     * Remove a Hologram
     *
     * @author KoddyDev
     * @return [Boolean] if removed
     */
    fun remove(): Boolean {
        holos.forEach { stand ->
            if(!stand.chunk.isLoaded) stand.chunk.load()
            if(!stand.isDead) stand.remove()
        }
        holos.clear()

        return true
    }

     */
}