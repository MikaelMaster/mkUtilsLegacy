package com.mikael.mkutilslegacy.bungee.command

import com.mikael.mkutilslegacy.api.toTextComponent
import com.mikael.mkutilslegacy.bungee.api.lib.ProxyCommand
import com.mikael.mkutilslegacy.bungee.api.utilsBungeeMain
import net.md_5.bungee.api.CommandSender

class BungeeVersionCommand : ProxyCommand("mkutilsproxy", "mkutilsbungee") {

    init {
        usage = "/mkutilsproxy"
        permission = null
    }

    override fun command(sender: CommandSender, args: List<String>) {
        sender.sendMessage("§a${utilsBungeeMain.systemName} §ev${utilsBungeeMain.description.version} §f- §bdeveloped with §c❤ §bby Mikael with help of Eduard and Koddy.".toTextComponent())
    }

}