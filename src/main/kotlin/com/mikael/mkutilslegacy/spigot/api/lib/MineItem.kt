package com.mikael.mkutilslegacy.spigot.api.lib

import com.mikael.mkutilslegacy.spigot.api.lib.book.MineBook
import com.mikael.mkutilslegacy.spigot.api.util.MineNBT
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.eduard.api.lib.game.EnchantGlow
import net.eduard.api.lib.storage.StorageAPI
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools
import net.minecraft.server.v1_8_R3.NBTTagCompound
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.SkullType
import org.bukkit.block.CreatureSpawner
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionType
import org.json.JSONObject
import java.io.*
import java.util.*
import org.apache.commons.codec.binary.Base64 as aBase64

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
 * - Also, [MineItem.fromJSONObject] is an option. (See [MineItem.toJSONObject] to learn how to transform a [MineItem] into a [JSONObject])
 *
 * @param item the [ItemStack] to create a new [MineItem]. *You can also use the others constructors below.*
 * @author Mikael
 * @author KoddyDev
 * @see ItemStack
 * @see MineNBT.Item
 */
open class MineItem(item: ItemStack) : ItemStack(item), Serializable {
    companion object {

        /**
         * Returns a new [MineItem] created using the properties in the given [json].
         *
         * @see MineItem.toJSONObject
         * @author Mikael
         */
        // @Deprecated("You should now use Base64 methods instead of JSON.")
        fun fromJSONObject(json: JSONObject): MineItem {
            val item = MineItem(Material.valueOf(json.getString("material")), json.getInt("amount"))
            item.data(json.getInt("data"))
            item.name(json.getString("name"))
            item.lore(json.getJSONArray("lore").map { it as String })
            item.addFlags(*json.getJSONArray("flags").map { ItemFlag.valueOf(it as String) }.toTypedArray())
            item.glowed(json.getBoolean("glowed"))

            val enchantsObj = json.getJSONObject("enchants")
            enchantsObj.keySet().forEach { enchant ->
                item.addEnchant(Enchantment.getByName(enchant), enchantsObj.getInt(enchant))
            }

            val extrasObj = json.getJSONObject("extras")
            if (extrasObj.has("skinUrl")) {
                item.skin(extrasObj.getString("skinUrl"))
            }
            if (extrasObj.has("skullName")) {
                item.skull(extrasObj.getString("skullName"))
            }

            return item
        }

        /**
         * Transforms a [Base64] String to a [MineItem]?.
         *
         * @param base64 The base64, is obvious.
         * @author KoddyDev
         * @return a new [MineItem]?.
         */
        fun fromBase64(base64: String): MineItem? {
            return try {
                val bytes = aBase64.decodeBase64(base64)
                val inputStream = ByteArrayInputStream(bytes)
                val dataInputStream = DataInputStream(inputStream)
                val nbtTagCompound = NBTCompressedStreamTools.a(dataInputStream)
                val nmsItemStack = net.minecraft.server.v1_8_R3.ItemStack.createStack(nbtTagCompound)
                MineItem(CraftItemStack.asBukkitCopy(nmsItemStack))
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }

    }

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
        meta.addItemFlags(*ItemFlag.entries.toTypedArray())
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
        meta.removeItemFlags(*ItemFlag.entries.toTypedArray())
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

    var isGlowed = false

    /**
     * Sets the [EnchantGlow] of this [MineItem].
     *
     * @param glowed if this item will be glowed or not.
     * @return this [MineItem].
     */
    fun glowed(glowed: Boolean): MineItem {
        isGlowed = if (glowed) {
            addEnchant(EnchantGlow.getGlow(), 1)
            true
        } else {
            removeEnchant(EnchantGlow.getGlow())
            false
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

    fun toMineNBTItem(): MineNBT.Item {
        return MineNBT.Item(this)
    }

    /**
     * @return a clone of this [MineItem].
     * @see ItemStack.clone
     */
    override fun clone(): MineItem {
        return super.clone() as MineItem
    }

    /**
     * Returns a [JSONObject] representation of this [MineItem].
     *
     * WARNING: if this [MineItem] uses one option set by one of the methods bellow, these options
     * will NOT be saved to the [JSONObject].
     *
     * - [MineItem.color]
     * - [MineItem.potion]
     * - [MineItem.spawnerType]
     *
     * If this is a [Material.SKULL_ITEM] with a [skinURL] or a [skullName] set, these properties will be saved.
     * In the future the other custom properties such as [spawnerType] will be saved too.
     *
     * @return a [JSONObject] representation of this [MineItem].
     * @author Mikael
     * @see MineItem.fromJSONObject
     */
    fun toJSONObject(): JSONObject {
        val json = JSONObject()
        json.put("material", this.type.name)
        json.put("amount", this.amount)
        json.put("data", this.durability.toInt()) // durability = material data
        json.put("name", getName())
        json.put("lore", getLore())
        json.put("flags", this.itemMeta.itemFlags.map { it.name })
        json.put("glowed", isGlowed)

        val enchantsObj = JSONObject()
        this.enchantments.forEach { (enchant, level) ->
            enchantsObj.put(enchant.name, level)
        }
        json.put("enchants", enchantsObj)

        val extrasObj = JSONObject()
        val skinUrl = this.skinURL
        skinUrl?.let {
            extrasObj.put("skinUrl", it)
        }
        val skullName = this.skullName
        skullName?.let {
            extrasObj.put("skullName", it)
        }
        json.put("extras", extrasObj)

        return json
    }

    /**
     * Transforms a [MineItem] to a [Base64]
     *
     * @author KoddyDev
     */
    fun toBase64(): String {
        val nmsItemStack: net.minecraft.server.v1_8_R3.ItemStack = CraftItemStack.asNMSCopy(this)
        val nbtTagCompound = if (nmsItemStack.hasTag()) nmsItemStack.tag else NBTTagCompound()
        nmsItemStack.save(nbtTagCompound)

        val byteArrayOutputStream = ByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(byteArrayOutputStream)
        NBTCompressedStreamTools.a(nbtTagCompound, dataOutputStream as DataOutput)

        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray())
    }

    // Change Custom Item Properties Functions below

    /**
     * Sets the color in this [MineItem].
     *
     * This should only be used when the [MineItem.getType] is something
     * that can have a color like a [Material.LEATHER_CHESTPLATE].
     *
     * @param color the new [Color] to be set.
     * @return this [MineItem].
     */
    fun color(color: Color): MineItem { // Change Item Color
        if (!this.type.name.startsWith("LEATHER_")) {
            this.type = Material.LEATHER_CHESTPLATE
        }
        if (this.itemMeta == null) return this
        val meta = this.itemMeta as LeatherArmorMeta
        meta.color = color
        this.itemMeta = meta
        return this
    }

    /**
     * Sets banners patterns in this [MineItem].
     *
     * @return this [MineItem].
     */
    fun banner(baseColor: DyeColor, patternColor: DyeColor, patternType: PatternType): MineItem { // Change Banners
        this.type = Material.BANNER
        if (this.itemMeta == null) return this
        val meta = this.itemMeta as BannerMeta
        meta.baseColor = baseColor
        meta.addPattern(Pattern(patternColor, patternType))
        this.itemMeta = meta
        return this
    }

    /**
     * Add a banner pattern in this [MineItem].
     *
     * @return this [MineItem].
     */
    fun addBanner(patternColor: DyeColor, patternType: PatternType): MineItem { // Change Banners
        this.type = Material.BANNER
        if (this.itemMeta == null) return this
        val meta = this.itemMeta as BannerMeta
        meta.addPattern(Pattern(patternColor, patternType))
        this.itemMeta = meta
        return this
    }

    /**
     * Builds a banner with the given [alphabet] letter.
     *
     * @return this [MineItem]
     */
    fun banner(alphabet: String, baseColor: DyeColor, dyeColor: DyeColor): MineItem {
        val letter = alphabet.uppercase()
        this.type = Material.BANNER
        if (this.itemMeta == null) return this
        val meta = this.itemMeta as BannerMeta
        meta.baseColor = baseColor
        when (letter) {
            "A" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_MIDDLE))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "B", "8" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_MIDDLE))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "C" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "D" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(baseColor, PatternType.CURLY_BORDER))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "E" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_MIDDLE))
                meta.addPattern(Pattern(baseColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "F" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_MIDDLE))
                meta.addPattern(Pattern(baseColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "G" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(baseColor, PatternType.HALF_HORIZONTAL))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "H" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_MIDDLE))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "I" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_CENTER))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "J" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(baseColor, PatternType.HALF_HORIZONTAL))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "K" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_MIDDLE))
                meta.addPattern(Pattern(baseColor, PatternType.HALF_VERTICAL_MIRROR))
                meta.addPattern(Pattern(dyeColor, PatternType.CROSS))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "L" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "M" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.TRIANGLE_TOP))
                meta.addPattern(Pattern(baseColor, PatternType.TRIANGLES_TOP))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "N" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(baseColor, PatternType.DIAGONAL_RIGHT_MIRROR))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "O" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "P" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(baseColor, PatternType.HALF_HORIZONTAL_MIRROR))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_MIDDLE))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "Q" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
                meta.addPattern(Pattern(dyeColor, PatternType.SQUARE_BOTTOM_RIGHT))
            }

            "R" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(baseColor, PatternType.HALF_HORIZONTAL_MIRROR))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "S" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(baseColor, PatternType.RHOMBUS_MIDDLE))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
                meta.addPattern(Pattern(baseColor, PatternType.CURLY_BORDER))
            }

            "T" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_CENTER))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "U" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "V" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(baseColor, PatternType.TRIANGLES_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "W" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.TRIANGLE_BOTTOM))
                meta.addPattern(Pattern(baseColor, PatternType.TRIANGLES_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "X" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(baseColor, PatternType.STRIPE_CENTER))
                meta.addPattern(Pattern(dyeColor, PatternType.CROSS))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "Y" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.CROSS))
                meta.addPattern(Pattern(baseColor, PatternType.HALF_VERTICAL_MIRROR))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "Z" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "1" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.SQUARE_TOP_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_CENTER))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "2" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(baseColor, PatternType.RHOMBUS_MIDDLE))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "3" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_MIDDLE))
                meta.addPattern(Pattern(baseColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "4" -> {
                meta.addPattern(Pattern(baseColor, PatternType.HALF_HORIZONTAL))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(baseColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_MIDDLE))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "5" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT))
                meta.addPattern(Pattern(baseColor, PatternType.CURLY_BORDER))
                meta.addPattern(Pattern(dyeColor, PatternType.SQUARE_BOTTOM_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "6" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(baseColor, PatternType.HALF_HORIZONTAL))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_MIDDLE))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "7" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(baseColor, PatternType.DIAGONAL_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.SQUARE_BOTTOM_LEFT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "9" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(baseColor, PatternType.HALF_HORIZONTAL_MIRROR))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_MIDDLE))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }

            "0" -> {
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_TOP))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_RIGHT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_BOTTOM))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_LEFT))
                meta.addPattern(Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT))
                meta.addPattern(Pattern(baseColor, PatternType.BORDER))
            }
        }
        itemMeta = meta
        return this
    }

    @Suppress("DEPRECATION")
    fun potion(potionType: PotionType, level: Int, isSplash: Boolean = false, isExtended: Boolean = false): MineItem { // Change Item Potion
        if (this.type != Material.POTION) {
            this.type = Material.POTION
        }
        val name: Int = potionType.damageValue
        var damage: Short
        if (potionType == PotionType.WATER) {
            damage = 0
        } else {
            if (this.type == null) {
                damage = (if (name == 0) 8192 else name).toShort()
            } else {
                damage = ((level - 1).toShort())
                damage = (damage.toInt() shl 5).toShort()
                damage = (damage.toInt() or (potionType.damageValue.toShort()).toInt()).toShort()
            }
            if (isSplash) {
                damage = (damage.toInt() or 16384).toShort()
            }
            if (isExtended) {
                damage = (damage.toInt() or 64).toShort()
            }
        }
        this.durability = damage
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
     * The current URL being used in this [MineItem].
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
     * The current Name being used in this [MineItem].
     *
     * @see getSkullName
     */
    private var skullName: String? = null

    /**
     * @return the [skullName].
     */
    fun getSkullName(): String? {
        return skullName
    }

    /**
     * Sets the Skull 'Owner' of this [MineItem].
     *
     * The [Material] type of this [MineItem] will be transformed into a [Material.SKULL_ITEM] if this is not yet.
     *
     * @param skullName the new Skull 'Owner' to be set.
     * @return this [MineItem].
     */
    fun skull(skullName: String): MineItem { // Custom Skull Name
        this.type = Material.SKULL_ITEM; this.data(SkullType.PLAYER.ordinal)
        if (this.itemMeta == null) error("Cannot get ItemMeta")
        val meta = this.itemMeta as SkullMeta
        meta.owner = skullName
        this.itemMeta = meta
        this.skinURL = null; this.skullName = skullName
        return this
    }

    /**
     * @see skin
     * @see skinId
     */
    private fun texture(textureBase64: String): MineItem { // Custom Skull Texture
        this.type = Material.SKULL_ITEM; this.data(SkullType.PLAYER.ordinal)
        if (this.itemMeta == null) error("Cannot get ItemMeta")
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
     * The [Material] type of this [MineItem] will be transformed into a [Material.SKULL_ITEM] if this is not yet.
     *
     * @param skinUrl the new Skin URL to be set.
     * @return this [MineItem].
     */
    fun skin(skinUrl: String): MineItem { // Custom Skull Skin
        this.skinURL = skinUrl; this.skullName = null
        val encodedData =
            Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinUrl).toByteArray())
        return texture(String(encodedData)) // DON'T CHANGE IT! '.toString()' will NOT work
    }

    /**
     * @see skin
     */
    fun skinId(skinId: String): MineItem { // Custom Skull Skin ID
        return this.skin("http://textures.minecraft.net/texture/${skinId}")
    }
}