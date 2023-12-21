package com.mikael.mkutilslegacy.spigot.api.lib.menu.button

import com.mikael.mkutilslegacy.spigot.api.lib.MineItem
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Represents a [MenuPage] button.
 *
 * @param name the name ("id") of this button.
 * @author Mikael
 * @see MineMenu
 * @see MenuPage
 */
@Suppress("WARNINGS")
open class MenuButton(var name: String) {

    /**
     * Invokes a new [MenuButton] using 'null-name-button' as the param [name].
     */
    constructor() : this("null-name-button")

    // Button Properties - Start
    internal var positionX = 1
    internal var positionY = 1
    internal var fixed = false

    internal var inventories: MutableList<Inventory> = mutableListOf() // List of inventories that have this button
    internal var autoEffectiveSlot: Int? = null
    internal val effectiveSlot: Int
        get() {
            if (autoEffectiveSlot != null) return autoEffectiveSlot!!
            val y = if (this.positionY > 1) this.positionY - 1 else 0
            return y * 9 + positionX - 1
        }

    var icon: ItemStack? = MineItem(Material.BARRIER) // Default icon = Barrier; This can NOT be set to null (AIR)
    var click: ((InventoryClickEvent) -> Unit) = click@{ /* it = InventoryClickEvent */ } // Default click = Do nothing
    // Button Properties - End

    /**
     * @return the button [positionX].
     */
    fun getPositionX(): Int {
        return positionX
    }

    /**
     * @return the button [positionY].
     */
    fun getPositionY(): Int {
        return positionY
    }

    /**
     * Note: Fixed means that this button will be ignored by [MineMenu.isAutoAlignItems] option.
     *
     * @return True if this is a fixed button. Otherwise, false. ([fixed])
     */
    fun isFixed(): Boolean {
        return fixed
    }

    /**
     * @return True if this button is animated. Otherwise, false.
     * @see MenuAnimatedButton
     */
    fun isAnimated(): Boolean {
        return this is MenuAnimatedButton
    }

    /**
     * @return the list of [Inventory]ies holding this button.
     */
    fun getInventories(): List<Inventory> {
        return inventories
    }

    /**
     * Note: This can be null if this button is [fixed].
     *
     * @return the Auto Effective Slot of this button. ([autoEffectiveSlot]?)
     */
    fun getAutoEffectiveSlot(): Int? {
        return autoEffectiveSlot
    }

    /**
     * @return the Effective Slot of this button. ([effectiveSlot])
     */
    fun getEffectiveSlot(): Int {
        return effectiveSlot
    }

}