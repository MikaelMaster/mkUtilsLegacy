package com.mikael.mkutilslegacy.spigot.api.lib

import com.mikael.mkutilslegacy.spigot.api.lib.book.MineBook
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.eduard.api.lib.game.EnchantGlow
import net.eduard.api.lib.storage.StorageAPI
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.SkullType
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
 * IMPORTANT:
 *
 * * A [MineItem] should NOT be used in *val*s to save data.
 * * So, if you have a data class that have a *val* saving an Item, use [ItemStack] instead.
 * * Use [MineItem] just as a constructor, since this can be given as an [ItemStack] in functions.
 * * The [StorageAPI] does not support save [MineItem]s yet. So, you can also use [ItemStack] to save items in storages. ([MineItem.toItemStack])
 *
 * To create/invoke a new MineItem you can use:
 * - MineItem(item: [ItemStack])
 * - MineItem(material: [Material])
 * - MineItem(material: [Material], amount: [Int])
 *
 * @param item the [ItemStack] to create a new [MineItem]. *You can also use the others constructors below.*
 * @author Mikael
 * @see ItemStack
 * @see MineNBT.Item
 */
open class MineItem(item: ItemStack) : ItemStack(item) {

    /**
     * Note: If you use this instead the method that asks for amount,
     * the new created [MineItem] will have *1* as amount.
     *
     * @param material the [Material] to create a new [MineItem].
     * @return a new [MineItem].
     */
    constructor(material: Material) : this(ItemStack(material))

    /**
     * @param material the [Material] to create a new [MineItem].
     * @param amount the amount ([Int] between 1 and 64) that the [MineItem] will have.
     * @return a new [MineItem].
     */
    constructor(material: Material, amount: Int) : this(ItemStack(material, amount))

    /**
     * Sets the name of this [MineItem].
     *
     * @param name the new name? to be set. Can be null.
     * @return this [MineItem].
     */
    fun name(name: String? = null): MineItem {
        val meta = this.itemMeta ?: return this
        meta.displayName = name
        this.itemMeta = meta
        return this
    }

    /**
     * @return this [MineItem] name.
     */
    fun getName(): String {
        return if (this.hasItemMeta()) if (this.itemMeta!!.hasDisplayName()) this.itemMeta!!.displayName else "" else ""
    }

    /**
     * Sets the lore of this [MineItem].
     *
     * @param lore the new lore to be set.
     * @return this [MineItem].
     */
    fun lore(vararg lore: String): MineItem {
        val meta = this.itemMeta ?: return this
        meta.lore = lore.toList()
        this.itemMeta = meta
        return this
    }

    /**
     * Sets the lore of this [MineItem].
     *
     * @param lore the new lore to be set.
     * @return this [MineItem].
     */
    fun lore(lore: List<String>): MineItem {
        val meta = this.itemMeta ?: return this
        meta.lore = lore
        this.itemMeta = meta
        return this
    }

    /**
     * Adds lines to this [MineItem] lore.
     *
     * @param lines the lines to be added.
     * @return this [MineItem].
     */
    fun addLore(vararg lines: String): MineItem {
        val meta = this.itemMeta ?: return this
        val newLore = mutableListOf<String>(); newLore.addAll(getLore())
        newLore.addAll(lines.toList())
        meta.lore = newLore
        this.itemMeta = meta
        return this
    }

    /**
     * Adds lines to this [MineItem] lore.
     *
     * @param lines the lines to be added.
     * @return this [MineItem].
     */
    fun addLore(lines: List<String>): MineItem {
        val meta = this.itemMeta ?: return this
        val newLore = mutableListOf<String>(); newLore.addAll(getLore())
        newLore.addAll(lines.toList())
        meta.lore = newLore
        this.itemMeta = meta
        return this
    }

    /**
     * Clears this [MineItem] lore.
     *
     * @return this [MineItem].
     */
    fun clearLore(): MineItem {
        val meta = this.itemMeta ?: return this
        meta.lore = listOf()
        this.itemMeta = meta
        return this
    }

    /**
     * @return this [MineItem] lore.
     */
    fun getLore(): List<String> {
        return if (this.hasItemMeta()) if (this.itemMeta!!.hasLore()) this.itemMeta!!.lore!! else emptyList() else emptyList()
    }

    /**
     * Sets the type of this [MineItem].
     *
     * @param material the new [Material] for this [MineItem].
     * @return this [MineItem].
     */
    fun type(material: Material): MineItem {
        this.type = material
        return this
    }

    /**
     * Sets the data of this [MineItem].
     *
     * IMPORTANT: the 'DATA' means the [ItemStack.durability]. So, the given [data] is converted to short.
     *
     * @param data the new data for this [MineItem].
     * @return this [MineItem].
     */
    fun data(data: Int): MineItem {
        this.durability = data.toShort() // durability = material data
        return this
    }

    /**
     * Sets the amount of this [MineItem].
     *
     * @param amount the new amount for this [MineItem].
     * @return this [MineItem].
     */
    fun amount(amount: Int): MineItem {
        this.amount = amount
        return this
    }

    /**
     * Adds the given [flags] to this [MineItem].
     *
     * @param flags the [ItemFlag](s) to be added.
     * @return this [MineItem].
     */
    fun addFlags(vararg flags: ItemFlag): MineItem {
        val meta = this.itemMeta ?: return this
        meta.addItemFlags(*flags)
        this.itemMeta = meta
        return this
    }

    /**
     * Adds all [ItemFlag]s to this [MineItem].
     *
     * @return this [MineItem].
     */
    fun addAllFlags(): MineItem {
        val meta = this.itemMeta ?: return this
        meta.addItemFlags(*ItemFlag.values())
        this.itemMeta = meta
        return this
    }

    /**
     * Removes the given [ItemFlag] from this [MineItem].
     *
     * @param flag the [ItemFlag] to be removed.
     * @return this [MineItem].
     */
    fun removeFlag(flag: ItemFlag): MineItem {
        val meta = this.itemMeta ?: return this
        meta.removeItemFlags(flag)
        this.itemMeta = meta
        return this
    }


    /**
     * Removes all the [ItemFlag]s from this [MineItem].
     *
     * @return this [MineItem].
     */
    fun removeFlags(): MineItem {
        val meta = this.itemMeta ?: return this
        meta.removeItemFlags(*ItemFlag.values())
        this.itemMeta = meta
        return this
    }

    /**
     * Adds the given [Enchantment] to this [MineItem].
     *
     * @param enchant the [Enchantment] to be added.
     * @return this [MineItem].
     */
    fun addEnchant(enchant: Enchantment, level: Int): MineItem {
        this.addUnsafeEnchantment(enchant, level)
        return this
    }

    /**
     * Removes the given [Enchantment] from this [MineItem].
     *
     * @param enchant the [Enchantment] to be removed.
     * @return this [MineItem].
     */
    fun removeEnchant(enchant: Enchantment): MineItem {
        this.removeEnchantment(enchant)
        return this
    }

    /**
     * Clears all the [Enchantment]s of this [MineItem].
     *
     * @return this [MineItem].
     */
    fun clearEnchants(): MineItem {
        for (enchant in this.enchantments.keys) {
            this.removeEnchantment(enchant)
        }
        return this
    }

    /**
     * Sets the [EnchantGlow] of this [MineItem].
     *
     * @param glowed if this item will be glowed or not.
     * @return this [MineItem].
     */
    fun glowed(glowed: Boolean): MineItem {
        if (glowed) {
            addEnchant(EnchantGlow.getGlow(), 1)
        } else {
            removeEnchant(EnchantGlow.getGlow())
        }
        return this
    }

    /**
     * @return this super class [ItemStack].
     * @see MineItem.clone
     */
    fun toItemStack(): ItemStack {
        return this.clone()
    }

    /**
     * @return a new [MineBook] using this [MineItem] as 'baseItem'.
     * @see MineBook
     */
    fun toMineBook(): MineBook {
        return MineBook(this)
    }

    /**
     * @return a [com.mikael.mkutilslegacy.spigot.api.lib.MineNBT.Item] givin this [MineItem] as 'baseItem'.
     */
    val nbtItem get() = MineNBT.Item(this)

    /**
     * @return a clone of this [MineItem].
     * @see ItemStack.clone
     */
    override fun clone(): MineItem {
        return super.clone() as MineItem
    }

    // Change Custom Item Properties Functions

    /**
     * Sets the color os this [MineItem].
     *
     * This should only be used when the [MineItem.getType] is something
     * that can have a color like a [Material.LEATHER_CHESTPLATE].
     *
     * @param color the new [Color] to be set.
     * @return this [MineItem].
     */
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

    /**
     * Sets the potion effect of this [MineItem].
     *
     * This should only be used when the [MineItem.getType] is a [Material.POTION].
     *
     * @param effect the new [PotionEffect] to be set.
     * @return this [MineItem].
     */
    fun potion(effect: PotionEffect): MineItem { // Change Item Potion Effect
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

    /**
     * Sets the entity type of this [MineItem].
     *
     * This should only be used when the [MineItem.getType] is a [Material.MOB_SPAWNER].
     *
     * @param type the new [EntityType] to be set.
     * @return this [MineItem].
     */
    fun spawnerType(type: EntityType): MineItem { // Change Item Spawner Type
        this.type = Material.MOB_SPAWNER
        if (this.itemMeta == null) return this
        val meta = this.itemMeta as BlockStateMeta
        val state = meta.blockState
        val spawner = state as CreatureSpawner
        spawner.spawnedType = type
        this.itemMeta = meta
        return this
    }

    /**
     * The current URL being use in this [MineItem].
     *
     * @see getSkinURL
     */
    private var skinURL: String? = null // Custom Skull Skin

    /**
     * @return the [skinURL].
     */
    fun getSkinURL(): String? {
        return skinURL
    }

    /**
     * Sets the Skull 'Owner' of this [MineItem].
     *
     * This should only be used when the [MineItem.getType] is a [Material.SKULL_ITEM].
     *
     * Note: When this is set, the [SkullType.PLAYER] will be set as the type.
     *
     * @param skullName the new Skull 'Owner' to be set.
     * @return this [MineItem].
     */
    fun skull(skullName: String): MineItem { // Custom Skull Name
        this.type = Material.SKULL_ITEM; this.data(SkullType.PLAYER.ordinal)
        if (this.itemMeta == null) return this
        val meta = this.itemMeta as SkullMeta
        meta.owner = skullName
        this.itemMeta = meta
        return this
    }

    /**
     * @see skin
     * @see skinId
     */
    private fun texture(textureBase64: String): MineItem { // Custom Skull Texture
        this.type = Material.SKULL_ITEM; this.data(SkullType.PLAYER.ordinal)
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

    /**
     * Sets the Skin URL of this [MineItem].
     *
     * This should only be used when the [MineItem.getType] is a [Material.SKULL_ITEM].
     *
     * @param skinUrl the new Skin URL to be set.
     * @return this [MineItem].
     */
    fun skin(skinUrl: String): MineItem { // Custom Skull Skin
        this.skinURL = skinUrl
        val encodedData =
            Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinUrl).toByteArray())
        return texture(String(encodedData)) // DON'T CHANGE IT! '.toString()' will NOT work
    }

    /**
     * @see skin
     */
    fun skinId(skinId: String): MineItem { // Custom Skull Skin ID
        return this.skin("https://textures.minecraft.net/texture/${skinId}")
    }
}