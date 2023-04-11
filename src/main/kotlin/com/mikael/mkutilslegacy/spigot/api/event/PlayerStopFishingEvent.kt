@file:Suppress("WARNINGS")

package com.mikael.mkutilslegacy.spigot.api.event

import com.mikael.mkutilslegacy.spigot.listener.GeneralListener
import org.bukkit.entity.Fish
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a player stops fishing.
 *
 * Can be cancelled since [org.bukkit.event.player.PlayerFishEvent.setCancelled] is available.
 *
 * @see GeneralListener.onChangeFishState
 * @see Player.isFishing
 */
class PlayerStopFishingEvent(val player: Player, val hook: Fish?) : Event(), Cancellable {
    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        private fun getHandlerList(): HandlerList = HANDLERS
    }

    private var cancelled = false

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(p0: Boolean) {
        cancelled = p0
    }
}