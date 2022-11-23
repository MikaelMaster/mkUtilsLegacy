package com.mikael.mkutilslegacy.spigot.api.lib.menu

import com.mikael.mkutilslegacy.spigot.api.lib.MineItem
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

open class MenuButton(var name: String) {

    constructor() : this("null-name-button")

    internal var positionX = 1
    internal var positionY = 1
    internal var fixed = false

    internal var isAnimated = false // Animated Button property
    internal var lastFrameId = 0 // Animated Button property
    var frames = mutableListOf<ItemStack>() // Animated Button property

    internal var inventory: Inventory? = null
    internal var menuId = 0
    internal var autoEffectiveSlot: Int? = null
    internal val effectiveSlot: Int
        get() {
            if (autoEffectiveSlot != null) return autoEffectiveSlot!!
            val y = if (this.positionY > 1) this.positionY - 1 else 0
            return y * 9 + positionX - 1
        }

    var icon: ItemStack? = frames.firstOrNull() ?: MineItem(Material.BARRIER) // default icon = Barrier; it can be set to null (AIR)
    var click: ((InventoryClickEvent) -> Unit) = click@{ /* it = InventoryClickEvent */ } // default click = do nothing

    @Deprecated("Deprecated since mkUtilsLegacy v1.2; Use the 'button{}' builder setter property instead.")
    open fun setPosition(x: Int, y: Int) {
        positionX = x
        positionY = y
    }

    fun getPositionX(): Int {
        return positionX
    }

    fun getPositionY(): Int {
        return positionY
    }

    fun isFixed(): Boolean {
        return fixed
    }

    fun isAnimated(): Boolean {
        return isAnimated
    }

    fun getInventory(): Inventory? {
        return inventory
    }

    fun getMenuId(): Int {
        return menuId
    }

    fun getAutoEffectiveSlot(): Int? {
        return autoEffectiveSlot
    }

    fun getEffectiveSlot(): Int {
        return effectiveSlot
    }
}