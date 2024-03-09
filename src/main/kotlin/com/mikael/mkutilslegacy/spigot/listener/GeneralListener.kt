package com.mikael.mkutilslegacy.spigot.listener

import com.mikael.mkutilslegacy.spigot.api.entitiesMKPluginQuickCheckList
import com.mikael.mkutilslegacy.spigot.api.event.PlayerStartFishingEvent
import com.mikael.mkutilslegacy.spigot.api.event.PlayerStopFishingEvent
import com.mikael.mkutilslegacy.spigot.api.lib.MineListener
import com.mikael.mkutilslegacy.spigot.api.runBlock
import com.mikael.mkutilslegacy.spigot.api.utilsMain
import net.eduard.api.lib.kotlin.mineCallEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.scheduler.BukkitRunnable

class GeneralListener : MineListener() {
    companion object {
        lateinit var instance: GeneralListener
    }

    init {
        instance = this@GeneralListener

        utilsMain.syncTimer(20 * 3, 20 * 3) { // Lists 'optimizer'
            invincibleEntities.removeIf { it.isDead }
            fishingPlayers.removeIf { !it.isOnline }

            /**
             * @see entitiesMKPluginQuickCheckList
             */
            entitiesMKPluginQuickCheckList.keys.removeIf { it.isDead }
        }
    }

    /**
     * Invincible Entity System below.
     *
     * @see Entity.isInvincible
     */
    internal val invincibleEntities = mutableSetOf<Entity>()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDamage(e: EntityDamageEvent) {
        if (!invincibleEntities.contains(e.entity)) return
        e.isCancelled = true
    }

    /**
     * Fishing Players System below.
     *
     * @see Player.isFishing
     */

    internal val fishingPlayers = mutableSetOf<Player>()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onChangeFishState(e: PlayerFishEvent) {
        val player = e.player
        player.runBlock {
            val fishState = e.state ?: return@runBlock
            val hook = e.hook
            when (fishState) {
                PlayerFishEvent.State.FISHING -> {
                    val playerStartFishEvent = PlayerStartFishingEvent(player, hook)
                    playerStartFishEvent.mineCallEvent() // Calls this event
                    if (!playerStartFishEvent.isCancelled) {
                        fishingPlayers.add(player)
                    } else {
                        e.isCancelled = true
                    }
                }

                else -> {
                    val playerStopFishEvent = PlayerStopFishingEvent(player, hook)
                    playerStopFishEvent.mineCallEvent() // Calls this event
                    if (!playerStopFishEvent.isCancelled) {
                        fishingPlayers.remove(player)
                    } else {
                        e.isCancelled = true
                    }
                }
            }
            object : BukkitRunnable() {
                override fun run() {
                    if (hook == null || hook.isDead || !fishingPlayers.contains(player)) {
                        val playerStopFishEvent = PlayerStopFishingEvent(player, hook)
                        playerStopFishEvent.mineCallEvent() // Calls this event
                        if (!playerStopFishEvent.isCancelled) {
                            fishingPlayers.remove(player)
                            cancel()
                        } else {
                            e.isCancelled = true
                        }
                    }
                }
            }.runTaskTimer(utilsMain, 5, 5)
        }
    }

}