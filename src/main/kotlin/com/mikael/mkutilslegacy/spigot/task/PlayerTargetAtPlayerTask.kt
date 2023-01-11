package com.mikael.mkutilslegacy.spigot.task

import com.mikael.mkutilslegacy.spigot.UtilsMain
import net.eduard.api.lib.event.PlayerTargetPlayerEvent
import net.eduard.api.lib.kotlin.mineCallEvent
import net.eduard.api.lib.manager.TimeManager
import net.eduard.api.lib.modules.Mine

internal class PlayerTargetAtPlayerTask : TimeManager(20L) {

    override fun run() {
        for (player in Mine.getPlayers()) {
            try {
                val target = Mine.getTarget(
                    player,
                    Mine.getPlayerAtRange(player.location, 100.0)
                ) ?: continue
                if (target.hasMetadata("NPC")) continue
                UtilsMain.instance.syncTask {
                    PlayerTargetPlayerEvent(
                        target, player
                    ).mineCallEvent()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

}