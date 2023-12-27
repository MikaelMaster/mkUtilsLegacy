package com.mikael.mkutilslegacy.spigot.api.map

import com.mikael.mkutilslegacy.spigot.api.map.map_1_8_R3.MapHelper_1_8_R3
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.map.MapView
import java.awt.image.BufferedImage

/**
 * [MapHelper] util class.
 *
 * This is based in the project 'Images' by Andavin: https://github.com/Andavin/Images
 *
 * @author Mikael
 * @see IMapHelper
 */
object MapHelper {

    private val helper_1_8_R3 = MapHelper_1_8_R3()

    fun getWorldMap(id: Int): MapView {
        return helper_1_8_R3.getWorldMap(id)
    }

     fun createMap(
        frameId: Int,
        mapId: Int,
        player: Player,
        location: Location,
        direction: BlockFace?,
        rotation: Int,
        pixels: ByteArray?
    ) {
         helper_1_8_R3.createMap(frameId, mapId, player, location, direction, rotation, pixels)
    }

    fun destroyMap(player: Player, frameIds: IntArray) {
        helper_1_8_R3.destroyMap(player, frameIds)
    }

    fun createPixels(image: BufferedImage): ByteArray {
        return helper_1_8_R3.createPixels(image)
    }

}