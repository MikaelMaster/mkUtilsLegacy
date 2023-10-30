package com.mikael.mkutilslegacy.spigot.api.lib.menu

import com.mikael.mkutilslegacy.spigot.api.lib.menu.button.MenuButton
import org.bukkit.inventory.Inventory

/**
 * Represents a [MineMenu] page.
 *
 * @author Mikael
 * @see MineMenu
 */
@Suppress("WARNINGS")
open class MenuPage {

    /**
     * This page ID.
     *
     * The page ID is generated based on each [MineMenu].
     * So, there will be a lot of [MenuPage]s with the same ID,
     * but always in different menus.
     */
    var pageId = 1

    /**
     * The [MenuButton]s inside this page.
     */
    val buttons = mutableSetOf<MenuButton>()

    /**
     * The [MineMenu] holder of this page.
     */
    var menu: MineMenu? = null

    /**
     * The [Inventory]; This page itself.
     */
    var inventory: Inventory? = null

    /**
     * The back (previous page) [MenuButton].
     */
    var backPageButton: MenuButton? = null

    /**
     * The back (previous) [MenuPage].
     */
    var backPage: MenuPage? = null

    /**
     * @return True if this page have a page before it. Otherwise, false.
     */
    val hasBackPage get() = backPage != null

    /**
     * The next page [MenuButton].
     */
    var nextPageButton: MenuButton? = null

    /**
     * The next [MenuPage].
     */
    var nextPage: MenuPage? = null

    /**
     * @return True if this page have a page after it. Otherwise, false.
     */
    val hasNextPage get() = nextPage != null

}