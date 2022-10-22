package com.mikael.mkutilslegacy.spigot.task

import com.mikael.mkutilslegacy.spigot.UtilsMain
import com.mikael.mkutilslegacy.spigot.api.openedMineMenu
import net.eduard.api.lib.manager.TimeManager
import net.eduard.api.lib.menu.getMenu
import net.eduard.api.lib.modules.Mine

class AutoUpdateMenusTask : TimeManager(UtilsMain.instance.config.getLong("MenuAPI.autoUpdateTicks")) {

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

        for (player in Mine.getPlayers()) { // mkUtils new Menu System
            try {
                val menu = player.openedMineMenu ?: continue
                val pageOpened = menu.getPageOpened(player) ?: continue
                if (!menu.isAutoUpdate) continue
                menu.open(player, pageOpened)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}