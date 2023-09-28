@file:Suppress("ClassName")

package com.mikael.mkutilslegacy.spigot.api.npc.npc_1_8_R3

import com.mikael.mkutilslegacy.spigot.api.blockLoc
import com.mikael.mkutilslegacy.spigot.api.toCenterLocation
import net.minecraft.server.v1_8_R3.EntityArmorStand
import net.minecraft.server.v1_8_R3.Pathfinder
import net.minecraft.server.v1_8_R3.PathfinderNormal
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity
import org.bukkit.entity.ArmorStand
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

class NPCPathFinder_1_8_R3(private val startPoint: Location, private val targetPoint: Location) {
    private val pathfinderNormal = PathfinderNormal()

    init {
        pathfinderNormal.a(true)
        pathfinderNormal.b(true)
        pathfinderNormal.c(true)
        pathfinderNormal.d(true)
    }

    fun getPathToTarget(): List<Location>? {
        val from = startPoint.toCenterLocation()
        val to = targetPoint.toCenterLocation()
        if (from.world != to.world) error("Two different worlds, really?")

        val fromStand = from.world.spawn(from, ArmorStand::class.java)
        fromStand.isVisible = false
        fromStand.isCustomNameVisible = false
        fromStand.isSmall = true
        val toStand = to.world.spawn(to, ArmorStand::class.java)
        toStand.isVisible = false
        toStand.isCustomNameVisible = false
        toStand.isSmall = true

        val calcPath = Pathfinder(pathfinderNormal).a(
            (fromStand.world as CraftWorld).handle,
            (fromStand as CraftEntity).handle as EntityArmorStand,
            (toStand as CraftEntity).handle as EntityArmorStand,
            1000f
        )
        fromStand.remove()
        toStand.remove()

        if (calcPath == null) return null
        val locs = mutableListOf<Location>()
        for (i in 0 until calcPath.d()) {
            val point = calcPath.a(i)
            val pitchYaw = getPitchYaw(from, to)

            locs.add(
                Location(
                    to.world,
                    point.a.toDouble(), point.b.toDouble(), point.c.toDouble(),
                    pitchYaw.first, pitchYaw.second
                ).blockLoc
            )
        }

        return locs
    }

    private fun getPitchYaw(from: Location, to: Location): Pair<Float, Float> {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val dz = to.z - from.z
        val distance = sqrt(dx * dx + dy * dy + dz * dz)

        val pitch = asin(dy / distance).toFloat()
        val yaw = (-atan2(dx, dz)).toFloat()

        return Pair(pitch, yaw)
    }
}