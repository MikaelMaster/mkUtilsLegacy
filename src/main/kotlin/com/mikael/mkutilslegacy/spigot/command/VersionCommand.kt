package com.mikael.mkutilslegacy.spigot.command

import com.mikael.mkutilslegacy.spigot.api.lib.MineCommand
import com.mikael.mkutilslegacy.spigot.api.utilsMain
import org.bukkit.command.CommandSender

class VersionCommand : MineCommand("mkutils", "mkutilslegacy") {

    init {
        usage = "/mkutils"
        permission = null
    }

    override fun command(sender: CommandSender, args: List<String>) {
        sender.sendMessage("§a${utilsMain.systemName} §ev${utilsMain.description.version} §f- §bdeveloped with §c❤ §bby Mikael with help of Eduard and Koddy.")
    }

}