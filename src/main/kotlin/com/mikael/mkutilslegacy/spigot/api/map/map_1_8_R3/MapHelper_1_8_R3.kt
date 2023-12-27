@file:Suppress("ClassName", "PrivatePropertyName")

package com.mikael.mkutilslegacy.spigot.api.map.map_1_8_R3

import com.mikael.mkutilslegacy.spigot.api.map.IMapHelper
import com.mikael.mkutilslegacy.spigot.api.map.MapHelper
import net.eduard.api.lib.kotlin.mineSendPacket
import net.eduard.api.lib.modules.Extra
import net.minecraft.server.v1_8_R3.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.map.MapPalette
import org.bukkit.map.MapView
import java.awt.Color
import java.awt.image.BufferedImage
import java.lang.reflect.Field
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * [IMapHelper] for NMS 1_8_R3.
 *
 * You should use methods from [MapHelper] in your project.
 *
 * This is based in the project 'Images' by Andavin: https://github.com/Andavin/Images
 *
 * @author Mikael
 * @see IMapHelper
 * @see MapHelper
 */
class MapHelper_1_8_R3 : IMapHelper {

    private val DEFAULT_STARTING_ID: Int = 8000
    private val ENTITY_ID: Field = Extra.getField(Entity::class.java, "id")
    private val MAP_IDS: MutableMap<UUID, AtomicInteger> = HashMap(4)

    @Suppress("DEPRECATION")
    override fun getWorldMap(id: Int): MapView {
        return Bukkit.getMap(id.toShort())
    }

    override fun nextMapId(world: World): Int {
        return MAP_IDS.computeIfAbsent(world.uid) { AtomicInteger(DEFAULT_STARTING_ID) }.getAndIncrement()
    }

    override fun createMap(
        frameId: Int,
        mapId: Int,
        player: Player,
        location: Location,
        direction: BlockFace?,
        rotation: Int,
        pixels: ByteArray?
    ) {
        val item = ItemStack(Items.FILLED_MAP)
        item.data = mapId

        val frame = EntityItemFrame((player.world as CraftWorld).handle)
        frame.item = item // Must set this first to avoid updating surrounding blocks
        frame.setLocation(location.x, location.y, location.z, 0f, 0f)
        frame.setDirection(CraftBlock.blockFaceToNotch(direction))
        ENTITY_ID.set(frame, frameId)

        val spawnPacket = PacketPlayOutSpawnEntity(frame, 71, frame.direction.b())
        val position = frame.getBlockPosition()
        spawnPacket.a(MathHelper.d((position.x * 32).toFloat()))
        spawnPacket.b(MathHelper.d((position.y * 32).toFloat()))
        spawnPacket.c(MathHelper.d((position.z * 32).toFloat()))

        val connection = (player as CraftPlayer).handle.playerConnection
        connection.sendPacket(spawnPacket)
        connection.sendPacket(PacketPlayOutEntityMetadata(frame.id, frame.dataWatcher, true))
        connection.sendPacket(
            PacketPlayOutMap(
                mapId,
                3,
                emptyList(),
                pixels,
                0,
                0,
                128,
                128
            )
        )
    }

    override fun destroyMap(player: Player, frameIds: IntArray) {
        player.mineSendPacket(PacketPlayOutEntityDestroy(*frameIds))
    }

    @Suppress("DEPRECATION")
    override fun createPixels(image: BufferedImage): ByteArray {
        val pixelCount = image.width * image.height
        val pixels = IntArray(pixelCount)
        image.getRGB(0, 0, image.width, image.height, pixels, 0, image.width)
        val colors = ByteArray(pixelCount)
        for (i in 0..<pixelCount) {
            colors[i] = MapPalette.matchColor(Color(pixels[i], true))
        }
        return colors
    }

}