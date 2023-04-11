package com.mikael.mkutilslegacy.spigot.api.npc

import com.mikael.mkutilslegacy.spigot.api.npc.enums.NPCPathWalkState
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*

/**
 * Represents a [PlayerNPCAPI] Player NPC.
 */
interface PlayerNPC {

    /**
     * Players hoo can see this NPC, and should receive packet updates.
     */
    val holders: MutableSet<Player>

    /**
     * If this NPC should be spawned for new players who join the server.
     * When true, also new players will be added to [holders] in order to see the NPC.
     */
    var shouldSpawnForNewPlayers: Boolean

    /**
     * If this NPC should look at nearby players (<5 blocks).
     */
    var shouldLookNearbyPlayers: Boolean

    /**
     * @return the [Player] (Bukkit entity) representation of this NPC.
     */
    fun getPlayer(): Player

    /**
     * @return True if this NPC is spawned and added into the 'NMS World'. Otherwise, false.
     */
    fun isSpawned(): Boolean

    /**
     * Spawns this NPC at the given [location].
     *
     * @param location the location to spawn this NPC.
     * @param players the players who should see this NPC, and that will be added to [holders].
     */
    fun spawn(location: Location, players: List<Player>)

    /**
     * Sets the skin of this NPC using a Minecraft account name.
     *
     * @param skinName the name of the skin to get meta value and signature from Mojang API.
     */
    fun setSkin(skinName: String)

    /**
     * Sets the skin of this NPC using the given [skinValue] and [skinSignature].
     *
     * @param skinValue the Skin Value to set.
     * @param skinSignature the Skin Signature to set.
     */
    fun setSkin(skinValue: String, skinSignature: String)

    /**
     * Despawns this NPC from the NMS world, and send the packet that this NPC is gone for the given [players].
     * Also, the given [players] will be, of course, removed from [holders].
     *
     * @param players the players to receive the packet update.
     */
    fun despawn(players: List<Player> = holders.toList())

    /**
     * Teleports this NPC to the given [location].
     *
     * @param location the location to teleport this NPC.
     * @param players the players to receive packets update.
     */
    fun teleport(location: Location, players: List<Player> = holders.toList())

    /**
     * Sets the velocity of this NPC.
     *
     * @param vector the vector to set as the NMS 'velocity'.
     * @param players the players to receive packets update.
     */
    fun setVelocity(vector: Vector, players: List<Player> = holders.toList())

    /**
     * Makes this NPC look at the given [location].
     *
     * @param location the location that the NPC should look at.
     * @param players the players to receive packets update.
     */
    fun lookAt(location: Location, players: List<Player> = holders.toList())

    /**
     * Makes this NPC walks in a path, diverting from blocks until reach the [targetLoc].
     *
     * @param targetLoc the loc that the NPC should reach.
     * @param fast put True if the code should NOT 're-process' the path after each tick. Otherwise, false.
     * @param players the players to receive packets update.
     * @param callback a callback that will be called when one of the [NPCPathWalkState]s is changed.
     */
    fun walkTo(
        targetLoc: Location,
        fast: Boolean = false,
        players: List<Player> = holders.toList(),
        callback: (state: NPCPathWalkState) -> Unit
    )

    /**
     * Shows this NPC for the given [player].
     * This is useful with [showFor] to spawn and hide an NPC for just one player.
     *
     * @see hideFor
     */
    fun showFor(player: Player)

    /**
     * Hides this NPC from the given [player].
     * This is useful with [showFor] to spawn and hide an NPC for just one player.
     *
     * @see showFor
     */
    fun hideFor(player: Player)

    /**
     * Updates (re-send) packets with the NPC data for the given [player].
     */
    fun updateFor(player: Player)

    /**
     * Respawns this NPC for the given [players].
     * If nothing is given in param [players], [holders] will be used.
     */
    fun respawn(players: List<Player> = holders.toList())

    /**
     * @return the spawn [Location] of this NPC.
     */
    fun getSpawnLocation(): Location

    /**
     * Sets the name of this NPC.
     */
    fun setName(name: String)

    /**
     * @return the unique [UUID] of this NPC.
     */
    fun getId(): UUID

}