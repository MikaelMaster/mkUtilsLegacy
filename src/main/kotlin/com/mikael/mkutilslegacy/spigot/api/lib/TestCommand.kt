package com.mikael.mkutilslegacy.spigot.api.lib

import com.mikael.mkutilslegacy.spigot.command.VersionCommand
import com.mikael.mkutilslegacy.spigot.command.VersionSubCommand
import org.bukkit.entity.Player

class TestCommand : MineCommand("teste") {

    init {
        usage = "legal mt gay"
        registerSubCommand(VersionSubCommand())
    }

    override fun playerCommand(player: Player, args: List<String>) {
        player.sendMessage("§aTeste")
    }

}