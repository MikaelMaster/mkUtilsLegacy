package com.mikael.mkutilslegacy.bungee.command

import com.mikael.mkutilslegacy.bungee.UtilsBungeeMain
import net.eduard.api.lib.command.Command
import net.eduard.api.lib.hybrid.ISender

class BungeeVersionCommand : Command("mkutilsproxy", "mkutilsbungee") {

    private val versionMsg get() = "§a${UtilsBungeeMain.instance.systemName} §ev${UtilsBungeeMain.instance.description.version} §f- §bdeveloped with §c❤ §bby Mikael."

    init {
        usage = "/mkutilsproxy"
        permission = "mkutils.defaultperm"
        permissionMessage = versionMsg
    }

    override fun onCommand(sender: ISender, args: List<String>) {
        sender.sendMessage(versionMsg)
    }

}