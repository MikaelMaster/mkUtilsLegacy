package com.mikael.mkutilslegacy.spigot.api.lib.menu

import com.mikael.mkutilslegacy.spigot.api.openedMineMenu
import org.bukkit.entity.Player

object MenuSystem {

    /**
     * Registered [MineMenu]s.
     * @see MineMenu.registerMenu
     */
    val registeredMenus = mutableSetOf<MineMenu>()

    /**
     * All [Player]s with a [MineMenu] opened.
     * @see Player.openedMineMenu
     */
    val openedMenu = mutableMapOf<Player, MineMenu>()

    /**
     * All [Player]s with a [MenuPage] opened. It means the player is also with a [MineMenu] opened.
     * @see Player.openedMineMenuPage
     */
    val openedPage = mutableMapOf<Player, MenuPage>()

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
        val registeredMenusCopy = mutableSetOf<MineMenu>(); registeredMenusCopy.addAll(registeredMenus)
        registeredMenusCopy.forEach { it.unregisterMenu() }; registeredMenusCopy.clear()
        registeredMenus.clear()
        openedMenu.clear()
        openedPage.clear()
    }

}