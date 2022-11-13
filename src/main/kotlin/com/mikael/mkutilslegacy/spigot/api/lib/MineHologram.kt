package com.mikael.mkutilslegacy.spigot.api.lib

import com.mikael.mkutilslegacy.spigot.api.chunk
import net.eduard.api.lib.modules.Mine
import org.bukkit.Location
import org.bukkit.entity.ArmorStand

class MineHologram(val location: Location, private vararg val lines: String) {
    private val holos = mutableListOf<ArmorStand>()

    init {
        holos.clear()
    }

    /**
     * Create a Hologram in a Location
     *
     * @author KoddyDev
     * @return [MutableList] of [ArmorStand]
     */
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
     * @param line Line to update
     * @param text Text to update
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
}