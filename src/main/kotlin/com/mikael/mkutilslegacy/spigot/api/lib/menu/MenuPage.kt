package com.mikael.mkutilslegacy.spigot.api.lib.menu

import org.bukkit.inventory.Inventory

open class MenuPage {

    var pageId = 1
    val buttons = mutableSetOf<MenuButton>()
    var inventory: Inventory? = null

    var backPageButton: MenuButton? = null
    var backPage: MenuPage? = null
    var hasBackPage = false

    var nextPageButton: MenuButton? = null
    var nextPage: MenuPage? = null
    var hasNextPage = false

}