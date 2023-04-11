package com.mikael.mkutilslegacy.spigot.api.lib.menu

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
 */
object MenuSystem {

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

    /**
     * Internal.
     * Should be run when mkUtils enables.
     */
    internal fun onEnable() {
        onDisable() // Same as onDisable
    }

    /**
     * Internal.
     * Should be run when mkUtils disables.
     */
    internal fun onDisable() {
        registeredMenus.forEach { menu ->
            menu.buttons.values.forEach {
                it.forEach { button ->
                    button.runAnimationTask?.cancel()
                    button.runAnimationTask = null
                }
            }
        }
        val registeredMenusCopy = mutableSetOf<MineMenu>(); registeredMenusCopy.addAll(registeredMenus)
        registeredMenusCopy.forEach { it.unregisterMenu() }; registeredMenusCopy.clear()
        registeredMenus.clear()
        openedMenu.clear()
        openedPage.clear()
    }

}