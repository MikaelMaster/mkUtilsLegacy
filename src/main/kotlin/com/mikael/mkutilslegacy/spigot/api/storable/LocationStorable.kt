package com.mikael.mkutilslegacy.spigot.api.storable

import net.eduard.api.lib.storage.Storable
import org.bukkit.Bukkit
import org.bukkit.Location

class LocationStorable : Storable<Location> {

    override fun store(map: MutableMap<String, Any>, loc: Location) {
        map["world-name"] = loc.world!!.name
        map["x"] = loc.x
        map["y"] = loc.y
        map["z"] = loc.z
        map["pitch"] = loc.pitch
        map["yaw"] = loc.yaw
    }

    override fun restore(map: MutableMap<String, Any>): Location {
        val world = Bukkit.getWorld(map["world-name"].toString())!!
        val x = map["x"].toString().toDouble()
        val y = map["y"].toString().toDouble()
        val z = map["z"].toString().toDouble()
        val pitch = map["pitch"].toString().toFloat()
        val yaw = map["yaw"].toString().toFloat()
        return Location(world, x, y, z, yaw, pitch)
    }

}