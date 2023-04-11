package com.mikael.mkutilslegacy.spigot.api.util

import com.mikael.mkutilslegacy.spigot.UtilsMain
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt

/**
 * A util class to use [Vector]s a simple way.
 *
 * @author Unknown
 * @author Mikael
 */
object LocationUtil {
    private val AXIS = arrayOfNulls<BlockFace>(4)
    private val NOTCHES = EnumMap<BlockFace, Int>(BlockFace::class.java)
    private val RADIAL = listOf(
        BlockFace.WEST,
        BlockFace.NORTH_WEST,
        BlockFace.NORTH,
        BlockFace.NORTH_EAST,
        BlockFace.EAST,
        BlockFace.SOUTH_EAST,
        BlockFace.SOUTH,
        BlockFace.SOUTH_WEST
    )

    init {
        for (i in RADIAL.indices) NOTCHES[RADIAL[i]] = i
        for (i in AXIS.indices) AXIS[i] = RADIAL[i shl 1]
    }

    fun getYawToLocation(point1: Vector, point2: Vector): Float {
        val dx: Double = point2.x - point1.x
        val dz: Double = point2.z - point1.z
        var angle = Math.toDegrees(atan2(dz, dx)).toFloat() - 90
        if (angle < 0) angle += 360.0f
        return angle
    }

    @JvmOverloads
    fun yawToFace(yaw: Float, useSubCardinalDirections: Boolean = true): BlockFace? {
        return if (useSubCardinalDirections) RADIAL[(yaw / 45f).roundToInt() and 0x7] else AXIS[(yaw / 90f).roundToInt() and 0x3]
    }

    fun faceToYaw(face: BlockFace): Int {
        return wrapAngle(45 * faceToNotch(face))
    }

    private fun wrapAngle(angle: Int): Int {
        var wrappedAngle = angle
        while (wrappedAngle <= -180) wrappedAngle += 360
        while (wrappedAngle > 180) wrappedAngle -= 360
        return wrappedAngle
    }

    private fun faceToNotch(face: BlockFace): Int {
        val notch = NOTCHES[face]
        return notch ?: 0
    }

    fun isInAABB(vector: Vector, min: Vector, max: Vector): Boolean {
        val realMin = Vector.getMinimum(min, max)
        val realMax = Vector.getMaximum(min, max)
        return vector.isInAABB(realMin, realMax)
    }

    fun pushEntity(entity: Entity, force: Double, exact: Boolean) {
        if (exact) {
            entity.velocity = entity.location.direction.normalize().setY(0.01)
        }
        UtilsMain.instance.syncDelay(1) {
            if (exact) {
                entity.velocity = entity.location.direction.normalize().multiply(force).setY(0.01)
            } else {
                val entityLocClone = entity.location.clone()
                entityLocClone.pitch = -15f
                entity.velocity = entityLocClone.direction.normalize().multiply(force)
            }
        }
    }

    fun getOppositeYaw(face: BlockFace, originalYaw: Float): Float {
        if (originalYaw in -90f..0f && face == BlockFace.NORTH) return -180 + abs(originalYaw)
        var yaw = normalizeYaw(originalYaw)
        var yawBlock = normalizeYaw(faceToYaw(face).toFloat())
        if (yawBlock == 0f) yawBlock = 360f
        if (yaw < yawBlock) yaw *= -1f
        yaw += yawBlock * 2
        if (yaw > 360) yaw -= 360f
        return normalizeYaw(yaw)
    }

    fun getOppositePitch(originalPitch: Float): Float {
        return -originalPitch
    }

    /* // Useless?
    private fun between(value: Float, min: Float, max: Float): Boolean {
        return value in min..max
    }
     */

    private fun normalizeYaw(realYaw: Float): Float {
        var result = realYaw % 360
        if (result < 0) result += 360f
        return result
    }
}