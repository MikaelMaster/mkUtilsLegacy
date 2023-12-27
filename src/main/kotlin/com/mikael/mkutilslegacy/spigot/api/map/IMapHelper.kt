package com.mikael.mkutilslegacy.spigot.api.map

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.map.MapView
import java.awt.image.BufferedImage

/**
 * [IMapHelper] interface.
 *
 * This is based in the project 'Images' by Andavin: https://github.com/Andavin/Images
 *
 * @author Mikael
 * @see MapHelper
 */
interface IMapHelper {

    fun getWorldMap(id: Int): MapView

    fun nextMapId(world: World): Int

    fun createMap(
        frameId: Int,
        mapId: Int,
        player: Player,
        location: Location,
        direction: BlockFace?,
        rotation: Int,
        pixels: ByteArray?
    )

    fun destroyMap(player: Player, frameIds: IntArray)

    fun createPixels(image: BufferedImage): ByteArray

}