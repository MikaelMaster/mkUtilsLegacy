package com.mikael.mkutilslegacy.spigot.task

import com.mikael.mkutilslegacy.spigot.UtilsMain
import com.mikael.mkutilslegacy.spigot.api.lib.menu.MenuSystem
import net.eduard.api.lib.manager.TimeManager
import net.eduard.api.lib.menu.getMenu
import net.eduard.api.lib.modules.Mine

internal class AutoUpdateMenusTask : TimeManager(UtilsMain.instance.config.getLong("MenuAPI.autoUpdateTicks")) {

    override fun run() {
        for (player in Mine.getPlayers()) { // EduardAPI legacy Menu System
            try {
                val menu = player.getMenu() ?: continue
                val pageOpened = menu.getPageOpen(player)
                val inventory = player.openInventory.topInventory
                menu.update(inventory, player, pageOpened, false)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        for ((player, menu) in MenuSystem.openedMenu) { // mkUtils new Menu System
            try {
                if (!menu.isAutoUpdate) continue
                val pageOpened = menu.getPageOpened(player) ?: continue
                menu.open(player, pageOpened)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}