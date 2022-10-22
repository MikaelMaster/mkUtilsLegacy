package com.mikael.mkutilslegacy.spigot.api.lib.menu

import com.mikael.mkutilslegacy.spigot.api.lib.MineItem
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

open class MenuButton(var name: String) {

    constructor() : this("null-name-button")

    var positionX = 1
    var positionY = 1
    var fixed = false

    var menuId = 0
    var autoEffectiveSlot: Int? = null
    val effectiveSlot: Int
        get() {
            if (autoEffectiveSlot != null) return autoEffectiveSlot!!
            val y = if (this.positionY > 1) this.positionY - 1 else 0
            return y * 9 + positionX - 1
        }

    var icon: ItemStack? = MineItem(Material.BARRIER) // default icon = Barrier; it can be set to null (AIR)
    var click: ((InventoryClickEvent) -> Unit) = click@{ /* it = InventoryClickEvent */ } // default click = do nothing

    @Deprecated(
        "Deprecated since mkUtils v1.1; Use the click var and Unit {} instead.",
        ReplaceWith("click = clickName@{ Do something on click (it = InventoryClickEvent) }")
    )
    fun setupClick(setup: ((InventoryClickEvent) -> Unit)) {
        click = setup
    }

    open fun setPosition(x: Int, y: Int) {
        positionX = x
        positionY = y
    }
}