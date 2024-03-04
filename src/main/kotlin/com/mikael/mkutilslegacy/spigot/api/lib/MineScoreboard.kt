package com.mikael.mkutilslegacy.spigot.api.lib

import com.mikael.mkutilslegacy.spigot.UtilsMain
import com.mikael.mkutilslegacy.spigot.api.lib.MineScoreboard.bukkitScores
import com.mikael.mkutilslegacy.spigot.api.lib.MineScoreboard.removeScore
import com.mikael.mkutilslegacy.spigot.api.lib.MineScoreboard.setScore
import com.mikael.mkutilslegacy.spigot.api.utilsMain
import net.eduard.api.lib.kotlin.cut
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criterias
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.NameTagVisibility
import org.bukkit.scoreboard.Scoreboard

/**
 * [MineScoreboard] util class
 *
 * This is a util class to manage players' scoreboard by a simple way.
 *
 * To set a score in a player, use [setScore]. Each player have a [Scoreboard], use [bukkitScores] to get
 * a specific player Bukkit [Scoreboard].
 * When you use [setScore] and give title and lines, these parameters is only for that player, wich means
 * you can create 'per-player' scoreboards with this util class.
 *
 * To update a player score, just call [setScore] with the new title and lines, if you call [removeScore]
 * and then [setScore] the player will see the scoreboard 'blink'.
 *
 * @author Mikael
 * @see Scoreboard
 */
@Suppress("WARNINGS")
object MineScoreboard {
    private val bukkitScores = mutableMapOf<Player, Scoreboard>()
    private val lastScoreLines = mutableMapOf<Player, List<String>>()

    init {
        utilsMain.syncTimer(20 * 3, 20 * 3) {
            bukkitScores.keys.removeIf { !it.isOnline }
            lastScoreLines.keys.removeIf { !it.isOnline }
        }
    }

    fun setScore(player: Player, title: String, lines: List<String>, healthBarEnabled: Boolean): Scoreboard {
        val oldScore = bukkitScores[player]
        val lastLines = lastScoreLines[player]
        if (oldScore != null && lastLines != null) {
            val sidebar = oldScore.getObjective(DisplaySlot.SIDEBAR)
            if (sidebar != null && sidebar.displayName == title.cut(32) && lastLines == lines.map { it.cut(32) }) {
                return oldScore
            }
        }
        val newScore = oldScore ?: Bukkit.getScoreboardManager().newScoreboard
        bukkitScores[player] = newScore
        if (lastLines != null && lastLines.size != lines.size) {
            newScore.objectives.forEach {
                it.unregister()
            }
        }

        if (newScore.getEntryTeam(player.name) == null) {
            val team = newScore.registerNewTeam(player.name)
            team.addEntry(player.name)
            if (healthBarEnabled) {
                // Define a visibilidade da tag do nome para mostrar a barra de vida
                team.nameTagVisibility = NameTagVisibility.ALWAYS
            }
        }
        val objective = newScore.getObjective("sidebar") ?: newScore.registerNewObjective("sidebar", "dummy")
        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.displayName = title.cut(32)

        val builder = StringBuilder("§r")
        lines.forEachIndexed { index, line ->
            val finalLine = line.cut(32)
            if (lastLines != null && lastLines.size == lines.size) {
                val lastLine = lastLines.getOrNull(index)
                if (lastLine != null && lastLine != finalLine) {
                    newScore.resetScores(lastLine)
                }
            }
            val score = objective.getScore(
                if (finalLine == "") {
                    builder.append(" ")
                    builder.toString()
                } else finalLine
            )
            score.score = lines.size - index
        }
        lastScoreLines[player] = lines

        if (oldScore == null) {
            player.scoreboard = newScore
        }
        updateHealthBar(player, healthBarEnabled)
        return newScore
    }

    fun removeScore(player: Player) {
        player.scoreboard = null
        bukkitScores.remove(player)
    }

    private fun updateHealthBar(player: Player, isEnabled: Boolean) {
        val healthScoreboard = player.scoreboard ?: return
        var healthObjective = healthScoreboard.getObjective("health")
        if (healthObjective == null && isEnabled) {
            val newHealthObjective = healthScoreboard.registerNewObjective("health", Criterias.HEALTH)
            newHealthObjective.displaySlot = DisplaySlot.BELOW_NAME
            newHealthObjective.displayName = "§c❤"
            healthObjective = newHealthObjective
        } else if (healthObjective != null && !isEnabled) {
            healthObjective.unregister()
            healthObjective = null
        }
        healthObjective?.getScore(player.name)?.score = player.health.toInt()
    }
}