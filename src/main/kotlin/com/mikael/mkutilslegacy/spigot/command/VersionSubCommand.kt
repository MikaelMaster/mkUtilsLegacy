package com.mikael.mkutilslegacy.spigot.command

import com.mikael.mkutilslegacy.spigot.UtilsMain
import com.mikael.mkutilslegacy.spigot.api.lib.MineCommand
import com.mikael.mkutilslegacy.spigot.api.lib.TestCommand
import net.eduard.api.lib.manager.CommandManager
import org.bukkit.command.CommandSender

class VersionSubCommand : MineCommand("mkutils", "mkutilslegacy") {

    private val versionMsg get() = "§a${UtilsMain.instance.systemName} §ev${UtilsMain.instance.description.version} §f- §bdeveloped with §c❤ §bby Mikael."

    init {
        usage = "/mkutils"
        permissionMessage = versionMsg
    }

    override fun command(sender: CommandSender, args: List<String>) {
        sender.sendMessage(versionMsg)
    }

}