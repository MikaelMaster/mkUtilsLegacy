package com.mikael.mkutilslegacy.spigot.api.lib.menu.example

import com.mikael.mkutilslegacy.spigot.api.runCommand
import com.mikael.mkutilslegacy.spigot.api.soundClick
import net.eduard.api.lib.manager.CommandManager
import org.bukkit.entity.Player

class ExampleMenuCommand : CommandManager("mkutilsexamplemenu") {

    init {
        usage = "/mkutilsexamplemenu"
        permission = "mkutils.menu.cmd.exaple"
        permissionMessage = "§cYou don't have permission to use this command."
    }

    override fun playerCommand(player: Player, args: Array<String>) {
        player.runCommand {
            player.soundClick(2f, 2f)
            SinglePageExampleMenu.instance.open(player)
        }
    }

}