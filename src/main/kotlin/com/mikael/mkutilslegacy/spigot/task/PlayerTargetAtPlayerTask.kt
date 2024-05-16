package com.mikael.mkutilslegacy.spigot.task

import com.mikael.mkutilslegacy.spigot.api.npc.PlayerNPCAPI
import net.eduard.api.lib.event.PlayerTargetPlayerEvent
import net.eduard.api.lib.kotlin.mineCallEvent
import net.eduard.api.lib.manager.TimeManager
import net.eduard.api.lib.modules.Mine
import org.bukkit.entity.Player

internal class PlayerTargetAtPlayerTask : TimeManager(20L) {

    private val searchRadios = 35.0

    override fun run() {
        for (player in Mine.getPlayers()) {
            try {
                val loc = player.location
                val target = Mine.getTarget(
                    player,
                    loc.world.getNearbyEntities(loc, searchRadios, searchRadios, searchRadios)
                        .filterIsInstance<Player>()
                ) ?: continue
                if (target.hasMetadata("NPC") || PlayerNPCAPI.isNPC(target)) continue
                PlayerTargetPlayerEvent(
                    target, player
                ).mineCallEvent()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

}