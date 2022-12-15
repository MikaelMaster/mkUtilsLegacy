package com.mikael.mkutilslegacy.spigot.api.lib.menu

import org.bukkit.inventory.Inventory

open class MenuPage {

    var pageId = 1
    val buttons = mutableSetOf<MenuButton>()
    var menu: MineMenu? = null
    var inventory: Inventory? = null

    var backPageButton: MenuButton? = null
    var backPage: MenuPage? = null
    val hasBackPage get() = backPage != null

    var nextPageButton: MenuButton? = null
    var nextPage: MenuPage? = null
    val hasNextPage get() = nextPage != null

}