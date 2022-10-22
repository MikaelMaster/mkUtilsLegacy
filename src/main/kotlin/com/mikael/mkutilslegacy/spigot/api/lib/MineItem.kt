package com.mikael.mkutilslegacy.spigot.api.lib

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.CreatureSpawner
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.potion.PotionEffect
import java.util.*

/**
 * [MineItem] util class
 *
 * This class represents an [ItemStack].
 *
 * To create/invoke a new MineItem you can use:
 * - MineItem(item: [ItemStack])
 * - MineItem(material: [Material])
 * - MineItem(material: [Material], amount: [Int])
 *
 * @param item the [ItemStack] to create a new MineItem. You can also use the other constructors above.
 * @author Mikael
 * @see ItemStack
 */
class MineItem(item: ItemStack) : ItemStack(item) {

    constructor(material: Material) : this(ItemStack(material))
    constructor(material: Material, amount: Int) : this(ItemStack(material, amount))

    fun name(name: String? = null): MineItem {
        val meta = this.itemMeta ?: return this
        meta.displayName = name
        this.itemMeta = meta
        return this
    }

    fun getName(): String {
        return if (this.hasItemMeta()) if (this.itemMeta!!.hasDisplayName()) this.itemMeta!!.displayName else "" else ""
    }

    fun lore(vararg lore: String): MineItem {
        val meta = this.itemMeta ?: return this
        meta.lore = lore.toList()
        this.itemMeta = meta
        return this
    }

    fun lore(lore: List<String>): MineItem {
        val meta = this.itemMeta ?: return this
        meta.lore = lore
        this.itemMeta = meta
        return this
    }

    fun addLore(vararg lines: String): MineItem {
        val meta = this.itemMeta ?: return this
        val newLore = mutableListOf<String>(); newLore.addAll(getLore())
        newLore.addAll(lines.toList())
        meta.lore = newLore
        this.itemMeta = meta
        return this
    }

    fun addLore(lines: List<String>): MineItem {
        val meta = this.itemMeta ?: return this
        val newLore = mutableListOf<String>(); newLore.addAll(getLore())
        newLore.addAll(lines.toList())
        meta.lore = newLore
        this.itemMeta = meta
        return this
    }

    fun clearLore(): MineItem {
        val meta = this.itemMeta ?: return this
        meta.lore = listOf()
        this.itemMeta = meta
        return this
    }

    fun getLore(): List<String> {
        return if (this.hasItemMeta()) if (this.itemMeta!!.hasLore()) this.itemMeta!!.lore!! else emptyList() else emptyList()
    }

    fun type(material: Material): MineItem {
        this.type = material
        return this
    }

    fun amount(amount: Int): MineItem {
        this.amount = amount
        return this
    }

    fun addFlags(vararg flags: ItemFlag): MineItem {
        val meta = this.itemMeta ?: return this
        meta.addItemFlags(*flags)
        this.itemMeta = meta
        return this
    }

    fun addAllFlags(): MineItem {
        val meta = this.itemMeta ?: return this
        meta.addItemFlags(*ItemFlag.values())
        this.itemMeta = meta
        return this
    }

    fun removeFlag(flag: ItemFlag): MineItem {
        val meta = this.itemMeta ?: return this
        meta.removeItemFlags(flag)
        this.itemMeta = meta
        return this
    }

    fun removeFlags(): MineItem {
        val meta = this.itemMeta ?: return this
        meta.removeItemFlags(*ItemFlag.values())
        this.itemMeta = meta
        return this
    }

    fun addEnchant(enchant: Enchantment, level: Int): MineItem {
        this.addUnsafeEnchantment(enchant, level)
        return this
    }

    fun removeEnchant(enchant: Enchantment): MineItem {
        this.removeEnchantment(enchant)
        return this
    }

    fun clearEnchants(): MineItem {
        for (enchant in this.enchantments.keys) {
            this.removeEnchantment(enchant)
        }
        return this
    }

    override fun clone(): MineItem {
        return super.clone() as MineItem
    }

    // Change Custom Item Properties Functions

    fun color(color: Color): MineItem { // Change Item Color
        if (!this.type.name.contains("LEATHER")) {
            this.type = Material.LEATHER_CHESTPLATE
        }
        if (this.itemMeta == null) return this
        val meta = this.itemMeta as LeatherArmorMeta
        meta.color = color
        this.itemMeta = meta
        return this
    }

    fun potion(effect: PotionEffect): MineItem { // Change Item PotionEffect
        if (this.type != Material.POTION) {
            this.type = Material.POTION
        }
        if (this.itemMeta == null) return this
        val meta = this.itemMeta as PotionMeta
        meta.setMainEffect(effect.type)
        meta.addCustomEffect(effect, true)
        this.itemMeta = meta
        return this
    }

    fun spawnerType(type: EntityType): MineItem {
        this.type = Material.MOB_SPAWNER
        if (this.itemMeta == null) return this
        val meta = this.itemMeta as BlockStateMeta
        val state = meta.blockState
        val spawner = state as CreatureSpawner
        spawner.spawnedType = type
        this.itemMeta = meta
        return this
    }

    private var skinURL: String? = null // Custom Skull Skin

    fun getSkinURL(): String? {
        return skinURL
    }

    fun skull(skullName: String): MineItem { // Custom Skull Name
        this.type = Material.SKULL_ITEM
        if (this.itemMeta == null) return this
        val meta = this.itemMeta as SkullMeta
        meta.owner = skullName
        this.itemMeta = meta
        return this
    }

    private fun texture(textureBase64: String): MineItem { // Custom Skull Texture
        this.type = Material.SKULL_ITEM
        if (this.itemMeta == null) return this
        val meta = this.itemMeta as SkullMeta
        val profile = GameProfile(UUID.randomUUID(), null as String?)
        profile.properties.put("textures", Property("textures", textureBase64))
        try {
            val profileField = meta.javaClass.getDeclaredField("profile")
            profileField.isAccessible = true
            profileField[meta] = profile
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        this.itemMeta = meta
        return this
    }

    fun skin(skinUrl: String): MineItem { // Custom Skull Skin
        this.skinURL = skinUrl
        val encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinUrl).toByteArray())
        return texture(String(encodedData)) // DON'T CHANGE IT-- .toString() will NOT work
    }

    fun skinId(skinId: String): MineItem { // Custom Skull Skin ID
        return this.skin("https://textures.minecraft.net/texture/${skinId}")
    }
}