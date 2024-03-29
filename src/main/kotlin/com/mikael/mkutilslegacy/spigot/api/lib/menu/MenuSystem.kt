package com.mikael.mkutilslegacy.spigot.api.lib.menu

import com.mikael.mkutilslegacy.spigot.api.lib.menu.button.MenuAnimatedButton
import com.mikael.mkutilslegacy.spigot.api.lib.menu.button.MenuButton
import com.mikael.mkutilslegacy.spigot.api.openedMineMenu
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/**
 * The [MineMenu] System.
 *
 * @author Mikael
 * @see MineMenu
 * @see MenuPage
 * @see MenuButton
 * @see MenuAnimatedButton
 */
@Suppress("WARNINGS")
object MenuSystem {

    // Translations - Start
    var BACK_PAGE_BUTTON_DEFAULT_NAME = "§aPage %page%"
    var NEXT_PAGE_BUTTON_DEFAULT_NAME = "§aPage %page%"
    var ASYNC_BUTTON_LOADING_NAME = "§8Loading..."
    // Translations - End

    /**
     * All registered [MineMenu]s.
     *
     * @see MineMenu.registerMenu
     */
    val registeredMenus = mutableSetOf<MineMenu>()

    /**
     * All [Player]s with a [MineMenu] opened.
     *
     * @see Player.openedMineMenu
     */
    val openedMenu = mutableMapOf<Player, MineMenu>()

    /**
     * All [Player]s with a [MenuPage] opened.
     * It means the player is also with a [MineMenu] opened.
     *
     * @see Player.openedMineMenuPage
     */
    val openedPage = mutableMapOf<Player, MenuPage>()

    /**
     * Opens the given [menu] to the given [player]
     *
     * @param player the [Player] to open the given [menu].
     * @param menu the [MineMenu] to open to the given [player].
     * @return the opened [Inventory] for the player.
     * @see MineMenu.open
     */
    fun openMenu(player: Player, menu: MineMenu): Inventory {
        return menu.open(player)
    }

    /**
     * It'll check if the value returned from [Player.openedMineMenu] is not null.
     * If it's not, and the returned menu is the given [menu], the [player] have an opened [MineMenu].
     *
     * @return True if the given [player] is with the given [menu] opened. Otherwise, false.
     * @see Player.openedMineMenu
     */
    fun isMenuOpen(menu: MineMenu, player: Player): Boolean {
        val openedMenu = player.openedMineMenu
        return openedMenu != null && openedMenu == menu
    }

    // mkUtils onEnable
    internal fun onEnable() {
        onDisable() // Same as onDisable
    }

    // mkUtils onDisable
    internal fun onDisable() {
        registeredMenus.forEach { menu ->
            menu.buttons.values.forEach { buttons ->
                buttons.filterIsInstance<MenuAnimatedButton>().forEach { button ->
                    button.runAnimationTask?.cancel()
                    button.runAnimationTask = null
                }
            }
        }
        val registeredMenusCopy = registeredMenus.toSet() // Copy registeredMenus Set
        registeredMenusCopy.forEach { it.unregisterMenu() }
        registeredMenus.clear()
        openedMenu.clear()
        openedPage.clear()
    }

}