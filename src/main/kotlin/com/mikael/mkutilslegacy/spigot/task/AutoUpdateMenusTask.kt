package com.mikael.mkutilslegacy.spigot.task

import com.mikael.mkutilslegacy.spigot.UtilsMain
import com.mikael.mkutilslegacy.spigot.api.lib.menu.MenuSystem
import com.mikael.mkutilslegacy.spigot.api.runBlock
import net.eduard.api.lib.manager.TimeManager

internal class AutoUpdateMenusTask : TimeManager(UtilsMain.instance.config.getLong("MenuAPI.autoUpdateTicks")) {

    override fun run() {
        /* Not used anymore
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
         */

        for ((player, menu) in MenuSystem.openedMenu) { // mkUtils new Menu System
            if (!menu.isAutoUpdate) continue
            val pageOpened = menu.getPageOpened(player) ?: continue
            // if (player.openInventory == null) continue // Useless?
            player.runBlock {
                menu.open(player, pageOpened, true)
            }
        }
    }
}