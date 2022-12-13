package com.mikael.mkutilslegacy.spigot.api.lib

import net.minecraft.server.v1_8_R3.NBTTagCompound
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack

object MineNBT {
    class Item(mineItem: MineItem) {
        private val nmsItem: net.minecraft.server.v1_8_R3.ItemStack = CraftItemStack.asNMSCopy(mineItem.toItemStack())
        private var itemCompound = if (nmsItem.hasTag()) nmsItem.tag else NBTTagCompound()

        fun setString(key: String, value: String) {
            itemCompound.setString(key, value)
            nmsItem.tag = itemCompound
        }

        fun getString(key: String): String? {
            return itemCompound.getString(key)
        }

        fun setInt(key: String, value: Int) {
            itemCompound.setInt(key, value)
            nmsItem.tag = itemCompound
        }

        fun getInt(key: String): Int? {
            return itemCompound.getInt(key).toString().toIntOrNull()
        }

        fun setBoolean(key: String, value: Boolean) {
            itemCompound.setBoolean(key, value)
            nmsItem.tag = itemCompound
        }

        fun getBoolean(key: String): Boolean {
            return itemCompound.getBoolean(key)
        }

        fun setDouble(key: String, value: Double) {
            itemCompound.setDouble(key, value)
            nmsItem.tag = itemCompound
        }

        fun getDouble(key: String): Double? {
            return itemCompound.getDouble(key).toString().toDoubleOrNull()
        }

        fun setFloat(key: String, value: Float) {
            itemCompound.setFloat(key, value)
            nmsItem.tag = itemCompound
        }

        fun getFloat(key: String): Float? {
            return itemCompound.getFloat(key).toString().toFloatOrNull()
        }

        fun setLong(key: String, value: Long) {
            itemCompound.setLong(key, value)
            nmsItem.tag = itemCompound
        }

        fun getLong(key: String): Long? {
            return itemCompound.getLong(key).toString().toLongOrNull()
        }

        fun setShort(key: String, value: Short) {
            itemCompound.setShort(key, value)
            nmsItem.tag = itemCompound
        }

        fun getShort(key: String): Short? {
            return itemCompound.getShort(key).toString().toShortOrNull()
        }

        fun hasKey(key: String): Boolean {
            return itemCompound.hasKey(key)
        }

        fun removeKey(key: String) {
            itemCompound.remove(key)
            nmsItem.tag = itemCompound
        }

        fun build(): MineItem {
            return MineItem(CraftItemStack.asBukkitCopy(nmsItem))
        }
    }
    class Entity(entity: org.bukkit.entity.Entity) {
        private val nmsEntity: net.minecraft.server.v1_8_R3.Entity = (entity as CraftEntity).handle
        private var entityCompound = if (nmsEntity.nbtTag != null) nmsEntity.nbtTag else NBTTagCompound()

        fun setString(key: String, value: String) {
            nmsEntity.c(entityCompound)
            entityCompound.setString(key, value)
            nmsEntity.f(entityCompound)
        }

        fun getString(key: String): String? {
            return entityCompound.getString(key)
        }

        fun setInt(key: String, value: Int) {
            nmsEntity.c(entityCompound)
            entityCompound.setInt(key, value)
            nmsEntity.f(entityCompound)
        }

        fun getInt(key: String): Int? {
            return entityCompound.getInt(key).toString().toIntOrNull()
        }

        fun setBoolean(key: String, value: Boolean) {
            nmsEntity.c(entityCompound)
            entityCompound.setBoolean(key, value)
            nmsEntity.f(entityCompound)
        }

        fun getBoolean(key: String): Boolean {
            return entityCompound.getBoolean(key)
        }

        fun setDouble(key: String, value: Double) {
            nmsEntity.c(entityCompound)
            entityCompound.setDouble(key, value)
            nmsEntity.f(entityCompound)
        }

        fun getDouble(key: String): Double? {
            return entityCompound.getDouble(key).toString().toDoubleOrNull()
        }

        fun setFloat(key: String, value: Float) {
            nmsEntity.c(entityCompound)
            entityCompound.setFloat(key, value)
            nmsEntity.f(entityCompound)
        }

        fun getFloat(key: String): Float? {
            return entityCompound.getFloat(key).toString().toFloatOrNull()
        }

        fun setLong(key: String, value: Long) {
            nmsEntity.c(entityCompound)
            entityCompound.setLong(key, value)
            nmsEntity.f(entityCompound)
        }

        fun getLong(key: String): Long? {
            return entityCompound.getLong(key).toString().toLongOrNull()
        }

        fun setShort(key: String, value: Short) {
            nmsEntity.c(entityCompound)
            entityCompound.setShort(key, value)
            nmsEntity.f(entityCompound)
        }

        fun getShort(key: String): Short? {
            return entityCompound.getShort(key).toString().toShortOrNull()
        }

        fun hasKey(key: String): Boolean {
            return entityCompound.hasKey(key)
        }

        fun removeKey(key: String) {
            nmsEntity.c(entityCompound)
            entityCompound.remove(key)
            nmsEntity.f(entityCompound)
        }

        fun build(): org.bukkit.entity.Entity {
            nmsEntity.f(entityCompound)
            return nmsEntity.bukkitEntity
        }
    }
}