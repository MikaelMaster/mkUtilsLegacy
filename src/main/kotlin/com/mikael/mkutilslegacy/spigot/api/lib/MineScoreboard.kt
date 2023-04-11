package com.mikael.mkutilslegacy.spigot.api.lib

import com.mikael.mkutilslegacy.spigot.UtilsMain
import net.eduard.api.lib.kotlin.cut
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard

/**
 * [MineScoreboard] util class
 *
 * This is a util class to manage players' scoreboard by a simple way.
 *
 * To set a score in a player, use [setScore]. Each player have a [Scoreboard], use [bukkitScores] to get
 * a specific player Bukkit [Scoreboard].
 * When you use [setScore] and give title and lines, these parameters is only for that player, wich means
 * you can created 'per-player' scoreboards with this util class.
 *
 * To update a player score, just call [setScore] will the new title and lines, if you call [removeScore]
 * and then [setScore] the player will see the scoboard 'blink'.
 *
 * @author Mikael
 * @see Scoreboard
 */
@Deprecated("In-dev.")
@Suppress("WARNINGS")
open class MineScoreboard {

    /**
     * All Bukkit [Scoreboard]s set by player throwout [setScore].
     *
     * Offline players will be removed from this map.
     * Every 3s a timer verifies if one of players in map is offline.
     * If the player is offline, he's removed from the this map.
     */
    val bukkitScores = mutableMapOf<Player, Scoreboard>()

    init {
        UtilsMain.instance.syncTimer(20 * 3, 20 * 3) {
            bukkitScores.keys.removeIf { !it.isOnline }
        }
    }

    /**
     * Sets a [Scoreboard] with the given [title] and [lines] for the [player].
     *
     * @param player the player to set the scoboard.
     * @param title the new scoreboard 'title'.
     * @param lines the new scoreboard 'lines'.
     * @return a new [Scoreboard] linked to the given [player]. See [bukkitScores] for details.
     */
    fun setScore(player: Player, title: String, vararg lines: String): Scoreboard {
        val oldScore = bukkitScores[player]
        val newScore = bukkitScores.put(player, Bukkit.getScoreboardManager().newScoreboard)!!

        val team = newScore.registerNewTeam(player.name)
        val objective = newScore.getObjective(DisplaySlot.SIDEBAR)
        objective.displayName = title.cut(16)
        lines.forEach { line ->
            team.addEntry(line.cut(16))
        }

        player.scoreboard = newScore
        oldScore?.let { it.teams.firstOrNull { it.name == player.name }?.unregister() }
        return newScore
    }

    /**
     * Removes the scoreboard set in the given [player]. ([Player.setScoreboard]=null)
     *
     * @param player the player to remove his [Scoreboard].
     */
    fun removeScore(player: Player) {
        player.scoreboard = null
        bukkitScores.remove(player)
    }

}