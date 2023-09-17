package com.mikael.mkutilslegacy.spigot.command

import com.mikael.mkutilslegacy.spigot.UtilsMain
import com.mikael.mkutilslegacy.spigot.api.lib.MineCommand
import org.bukkit.command.CommandSender

class VersionCommand : MineCommand("mkutils", "mkutilslegacy") {

    init {
        usage = "/mkutils"
        permission = null
    }

    override fun command(sender: CommandSender, args: List<String>) {
        sender.sendMessage("§a${UtilsMain.instance.systemName} §ev${UtilsMain.instance.description.version} §f- §bdeveloped with §c❤ §bby Mikael with help of Eduard and Koddy.")

        /*
        if (sender !is Player) return
        val npc = PlayerNPCAPI.create(sender.world, lookNearbyPlayers = true)
        npc.setSkin(sender.name)
        npc.spawn(sender.location, listOf(sender))
        PlayerNPCAPI.hideHeadNick(npc)
        PlayerNPCAPI.setExtraHeadHolos(npc, MineHologram("§c§lAVENTUREIRO", "§eClique para abrir."))
        PlayerNPCAPI.setNPCClickAction(npc) {
            PlayerNPCAPI.showTextBalloon(
                npc, it.player, "Oi tudo bem? Me chamo Aventureiro e vou te ajudar nessa jornada! Qual seu nome... ${it.player.name}? Nome dahora, vamos nessa?"
            ) { balloonState ->
                if (balloonState == NPCTextBalloonState.FLUSH_CHARACTERS) return@showTextBalloon
                sender.sendMessage("Estado do balão de fala: ${balloonState.name}")
            }
        }
         */
    }

}