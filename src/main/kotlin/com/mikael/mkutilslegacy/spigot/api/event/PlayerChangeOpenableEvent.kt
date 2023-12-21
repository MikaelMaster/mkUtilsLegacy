@file:Suppress("WARNINGS")

package com.mikael.mkutilslegacy.spigot.api.event

import com.mikael.mkutilslegacy.spigot.listener.CustomEventsListener
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a player opens or close a openable block (door, trapdoor, etc).
 *
 * @see CustomEventsListener.onPlayerOpenOrCloseOpenable
 */
class PlayerChangeOpenableEvent(val player: Player, val changedBlock: Block, val toSate: OpenableState) : Event(), Cancellable {
    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        private fun getHandlerList(): HandlerList = HANDLERS
    }

    enum class OpenableState {
        OPEN, CLOSED;

        val asBoolean get() = if (this == OPEN) true else false
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