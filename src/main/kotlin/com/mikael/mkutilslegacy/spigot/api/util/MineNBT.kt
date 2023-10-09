package com.mikael.mkutilslegacy.spigot.api.util

import com.mikael.mkutilslegacy.spigot.api.lib.MineItem
import com.mikael.mkutilslegacy.spigot.api.toMineItem
import net.minecraft.server.v1_8_R3.NBTBase
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools
import net.minecraft.server.v1_8_R3.NBTTagCompound
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * mkUtils [MineNBT] util class
 *
 * @author KoddyDev
 * @author Mikael
 */
object MineNBT {

    /**
     * @param baseItem the [MineItem] to be used as a base.
     */
    class Item(baseItem: MineItem) {
        private val nmsItem: net.minecraft.server.v1_8_R3.ItemStack = CraftItemStack.asNMSCopy(baseItem.toItemStack())
        private var itemCompound = if (nmsItem.hasTag()) nmsItem.tag else NBTTagCompound()

        fun clearCompound(): MineNBT.Item {
            itemCompound.c().forEach { key ->
                itemCompound.remove(key)
            }
            nmsItem.tag = itemCompound
            return this@Item
        }

        fun getKeys(): List<String> {
            return itemCompound.c().toList()
        }

        fun getValues(): List<NBTBase> {
            return itemCompound.c().map { itemCompound.get(it) }
        }

        fun map(): Map<String, NBTBase> {
            return itemCompound.c().associateWith { itemCompound.get(it) }
        }

        fun base64(): String {
            val outputStream = ByteArrayOutputStream()
            NBTCompressedStreamTools.a(itemCompound, outputStream)
            return Base64.getEncoder().encodeToString(outputStream.toByteArray())
        }

        fun base64(base64: String): MineNBT.Item {
            val inputStream = ByteArrayInputStream(
                Base64.getDecoder().decode(base64)
            )
            val nbtTagCompound = NBTCompressedStreamTools.a(inputStream)
            inputStream.close()
            itemCompound = nbtTagCompound
            nmsItem.tag = nbtTagCompound
            return this@Item
        }


        fun setString(key: String, value: String): MineNBT.Item {
            itemCompound.setString(key, value)
            nmsItem.tag = itemCompound
            return this@Item
        }

        fun getString(key: String): String? {
            return itemCompound.getString(key)
        }

        fun setInt(key: String, value: Int): MineNBT.Item {
            itemCompound.setInt(key, value)
            nmsItem.tag = itemCompound
            return this@Item
        }

        fun getInt(key: String): Int? {
            return itemCompound.getInt(key).toString().toIntOrNull()
        }

        fun setBoolean(key: String, value: Boolean): MineNBT.Item {
            itemCompound.setBoolean(key, value)
            nmsItem.tag = itemCompound
            return this@Item
        }

        fun getBoolean(key: String): Boolean {
            return itemCompound.getBoolean(key)
        }

        fun setDouble(key: String, value: Double): MineNBT.Item {
            itemCompound.setDouble(key, value)
            nmsItem.tag = itemCompound
            return this@Item
        }

        fun getDouble(key: String): Double? {
            return itemCompound.getDouble(key).toString().toDoubleOrNull()
        }

        fun setFloat(key: String, value: Float): MineNBT.Item {
            itemCompound.setFloat(key, value)
            nmsItem.tag = itemCompound
            return this@Item
        }

        fun getFloat(key: String): Float? {
            return itemCompound.getFloat(key).toString().toFloatOrNull()
        }

        fun setLong(key: String, value: Long): MineNBT.Item {
            itemCompound.setLong(key, value)
            nmsItem.tag = itemCompound
            return this@Item
        }

        fun getLong(key: String): Long? {
            return itemCompound.getLong(key).toString().toLongOrNull()
        }

        fun setShort(key: String, value: Short): MineNBT.Item {
            itemCompound.setShort(key, value)
            nmsItem.tag = itemCompound
            return this@Item
        }

        fun getShort(key: String): Short? {
            return itemCompound.getShort(key).toString().toShortOrNull()
        }

        fun hasKey(key: String): Boolean {
            return itemCompound.hasKey(key)
        }

        fun removeKey(key: String): MineNBT.Item {
            itemCompound.remove(key)
            nmsItem.tag = itemCompound
            return this@Item
        }

        /**
         * @return the final [MineItem] with all values defined.
         */
        fun toMineItem(): MineItem {
            return CraftItemStack.asBukkitCopy(nmsItem).toMineItem()
        }
    }

    /**
     * @param entity the (Bukkit) [Entity] to be used as a base.
     */
    class Entity(entity: org.bukkit.entity.Entity) {
        private val nmsEntity: net.minecraft.server.v1_8_R3.Entity = (entity as CraftEntity).handle
        private var entityCompound = if (nmsEntity.nbtTag != null) nmsEntity.nbtTag else NBTTagCompound()

        fun setString(key: String, value: String): MineNBT.Entity {
            nmsEntity.c(entityCompound)
            entityCompound.setString(key, value)
            nmsEntity.f(entityCompound)
            return this@Entity
        }

        fun getString(key: String): String? {
            return entityCompound.getString(key)
        }

        fun setInt(key: String, value: Int): MineNBT.Entity {
            nmsEntity.c(entityCompound)
            entityCompound.setInt(key, value)
            nmsEntity.f(entityCompound)
            return this@Entity
        }

        fun getInt(key: String): Int? {
            return entityCompound.getInt(key).toString().toIntOrNull()
        }

        fun setBoolean(key: String, value: Boolean): MineNBT.Entity {
            nmsEntity.c(entityCompound)
            entityCompound.setBoolean(key, value)
            nmsEntity.f(entityCompound)
            return this@Entity
        }

        fun getBoolean(key: String): Boolean {
            return entityCompound.getBoolean(key)
        }

        fun setDouble(key: String, value: Double): MineNBT.Entity {
            nmsEntity.c(entityCompound)
            entityCompound.setDouble(key, value)
            nmsEntity.f(entityCompound)
            return this@Entity
        }

        fun getDouble(key: String): Double? {
            return entityCompound.getDouble(key).toString().toDoubleOrNull()
        }

        fun setFloat(key: String, value: Float): MineNBT.Entity {
            nmsEntity.c(entityCompound)
            entityCompound.setFloat(key, value)
            nmsEntity.f(entityCompound)
            return this@Entity
        }

        fun getFloat(key: String): Float? {
            return entityCompound.getFloat(key).toString().toFloatOrNull()
        }

        fun setLong(key: String, value: Long): MineNBT.Entity {
            nmsEntity.c(entityCompound)
            entityCompound.setLong(key, value)
            nmsEntity.f(entityCompound)
            return this@Entity
        }

        fun getLong(key: String): Long? {
            return entityCompound.getLong(key).toString().toLongOrNull()
        }

        fun setShort(key: String, value: Short): MineNBT.Entity {
            nmsEntity.c(entityCompound)
            entityCompound.setShort(key, value)
            nmsEntity.f(entityCompound)
            return this@Entity
        }

        fun getShort(key: String): Short? {
            return entityCompound.getShort(key).toString().toShortOrNull()
        }

        fun setByte(key: String, value: Byte): MineNBT.Entity {
            nmsEntity.c(entityCompound)
            entityCompound.setByte(key, value)
            nmsEntity.f(entityCompound)
            return this@Entity
        }

        fun getByte(key: String): Byte? {
            return entityCompound.getByte(key).toString().toByteOrNull()
        }

        fun hasKey(key: String): Boolean {
            return entityCompound.hasKey(key)
        }

        fun removeKey(key: String): MineNBT.Entity {
            nmsEntity.c(entityCompound)
            entityCompound.remove(key)
            nmsEntity.f(entityCompound)
            return this@Entity
        }

        /**
         * @return the final [Entity] with all values defined.
         */
         fun toBukkitEntity(): org.bukkit.entity.Entity {
            nmsEntity.f(entityCompound)
            return nmsEntity.bukkitEntity
        }
    }
}