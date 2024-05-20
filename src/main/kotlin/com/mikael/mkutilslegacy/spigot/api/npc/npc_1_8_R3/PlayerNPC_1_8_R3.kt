@file:Suppress("ClassName", "WARNINGS")

package com.mikael.mkutilslegacy.spigot.api.npc.npc_1_8_R3

import com.mikael.mkutilslegacy.spigot.api.npc.PlayerNPC
import com.mikael.mkutilslegacy.spigot.api.npc.PlayerNPCAPI
import com.mikael.mkutilslegacy.spigot.api.npc.enums.NPCPathWalkState
import com.mikael.mkutilslegacy.spigot.api.utilsMain
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.eduard.api.lib.kotlin.cut
import net.eduard.api.lib.kotlin.mineSendPacket
import net.eduard.api.lib.modules.Extra
import net.eduard.api.lib.modules.FakePlayer
import net.eduard.api.lib.modules.Mine
import net.minecraft.server.v1_8_R3.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.craftbukkit.v1_8_R3.CraftServer
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.NameTagVisibility
import org.bukkit.util.Vector
import java.util.*

class PlayerNPC_1_8_R3(
    var npcName: String,
    val npcWorld: World
) : PlayerNPC {
    val nmsWorld: WorldServer = (npcWorld as CraftWorld).handle
    val nmsServer: DedicatedPlayerList = (Bukkit.getServer() as CraftServer).handle
    val npcInteractManager = PlayerInteractManager(nmsWorld)
    val npcId: UUID = FakePlayer(Extra.newKey(Extra.KeyType.LETTER, 16)).uniqueId
    val npcProfile = GameProfile(npcId, npcName.cut(16))
    val npc = EntityPlayer(nmsServer.server, nmsWorld, npcProfile, npcInteractManager)
    var npcLocation = npcWorld.spawnLocation!!
    override val holders = mutableSetOf<Player>()
    override var shouldSpawnForNewPlayers = true
    override var shouldLookNearbyPlayers = false

    init {
        npc.playerConnection = PlayerConnection_1_8_R3(npc)

        // Sets that all NPC skin parts is enabled, including the cloak.
        npc.dataWatcher.watch(10, 127.toByte())
    }

    override fun getPlayer(): Player {
        return npc.bukkitEntity
    }

    override fun isSpawned(): Boolean {
        return !npc.bukkitEntity.isDead
    }

    override fun teleport(location: Location, players: List<Player>) {
        // if (!isSpawned()) return
        this.npcLocation = location
        npc.pitch = npcLocation.pitch
        npc.yaw = npcLocation.yaw
        npc.f(npcLocation.yaw) // Sets NPC head rotation
        npc.setLocation(npcLocation.x, npcLocation.y, npcLocation.z, npcLocation.yaw, npcLocation.pitch)
        val packetNpcTeleport = PacketPlayOutEntityTeleport(npc)
        val packetPlayerUpdateMetadata = PacketPlayOutEntityMetadata(npc.id, npc.dataWatcher, true)
        val packetPlayerHeadRotation = PacketPlayOutEntityHeadRotation(npc, MathHelper.d(npc.headRotation * 256.0f / 360.0f).toByte())
        players.forEach { player ->
            val playerConnection = (player as CraftPlayer).handle.playerConnection
            playerConnection.sendPacket(packetNpcTeleport)
            playerConnection.sendPacket(packetPlayerUpdateMetadata)
            playerConnection.sendPacket(packetPlayerHeadRotation)
        }
    }

    override fun setVelocity(vector: Vector, players: List<Player>) {
        val packetNpcVelocity = PacketPlayOutEntityVelocity(npc.id, vector.x, vector.y, vector.z)
        players.forEach { player ->
            player.mineSendPacket(packetNpcVelocity)
        }
    }

    override fun lookAt(location: Location, players: List<Player>) {
        // if (!isSpawned()) return
        val newLoc = this.npcLocation.clone()
        newLoc.direction = location.toVector().subtract(this.npcLocation.toVector()).normalize()
        teleport(newLoc, players)
    }

    override fun walkTo(
        targetLoc: Location,
        fast: Boolean,
        players: List<Player>,
        callback: (state: NPCPathWalkState) -> Unit
    ) {
        var state = NPCPathWalkState.WAITING_PATH
        callback.invoke(state)
        var path: List<Location>? = NPCPathFinder_1_8_R3(npcLocation, targetLoc).getPathToTarget()
        var currentPathIndex = 0
        object : BukkitRunnable() {
            override fun run() {
                if (state == NPCPathWalkState.WAITING_PATH) {
                    state = NPCPathWalkState.STARTED
                    callback.invoke(state)
                }
                if (!fast) {
                    path = NPCPathFinder_1_8_R3(npcLocation, targetLoc).getPathToTarget()
                }
                val nextPathStep = path?.getOrNull(currentPathIndex)
                if (path == null || nextPathStep == null) {
                    state = NPCPathWalkState.FAILED
                    callback.invoke(state)
                    cancel()
                    return
                }

                nextPathStep.direction = nextPathStep.toVector().subtract(npcLocation.toVector()).normalize()
                // teleport(nextPathStep, holders.toList())
                val moveTarget = nextPathStep.toVector().subtract(npcLocation.toVector())
                npc.move(moveTarget.x, moveTarget.y, moveTarget.z)

                state = NPCPathWalkState.FLUSH_LOCATION
                callback.invoke(state)

                if (currentPathIndex != path!!.lastIndex) {
                    currentPathIndex++
                    return
                }
                state = NPCPathWalkState.COMPLETED
                callback.invoke(state)
                cancel()
            }
        }.runTaskTimer(utilsMain, 1, 5)
    }

    override fun spawn(location: Location, players: List<Player>) {
        this.npcLocation = location
        npc.pitch = npcLocation.pitch
        npc.yaw = npcLocation.yaw
        npc.f(npcLocation.yaw) // Sets NPC head rotation
        npc.setLocation(npcLocation.x, npcLocation.y, npcLocation.z, npcLocation.yaw, npcLocation.pitch)
        /*
        if (entity is EntityHuman) {
            val entityhuman = entity as EntityHuman
            this.players.add(entityhuman)
            this.everyoneSleeping()
        }
        this.getChunkAt(i, j).a(entity)
        this.entityList.add(entity)
        this.a(entity)
        */
        //val i = MathHelper.floor(npc.locX / 16.0)
        //val j = MathHelper.floor(npc.locZ / 16.0)
        // nmsWorld.entityList.add(npc)
        //nmsWorld.a(npc)
        // nmsWorld.getChunkAt(i, j).a(npc)
        /*
        this.entitiesById.a(entity.getId(), entity)
        this.entitiesByUUID.put(entity.getUniqueID(), entity)
        val aentity: Array<Entity> = entity.aB()
        if (aentity != null) {
            for (i in aentity.indices) {
                this.entitiesById.a(aentity[i].id, aentity[i])
            }
        }
        */
        nmsWorld.addEntity(npc, CreatureSpawnEvent.SpawnReason.CUSTOM)
        npc.bukkitEntity.setMetadata("NPC", FixedMetadataValue(utilsMain, true))
        // npc.listName = null
        for (player in players.filter { p -> !PlayerNPCAPI.isNPC(p) }) {
            holders.add(player)
            hideFor(player)
            showFor(player, onlyFlush = false)
        }
    }

    override fun setSkin(skinName: String) {
        val uuid = Extra.getPlayerUUIDByName(skinName)
        val skinProperty = Extra.getSkinProperty(uuid)
        val skinValue = skinProperty.get("value").asString
        val skinSignature = skinProperty.get("signature").asString
        setSkin(skinValue, skinSignature)
    }

    override fun setSkin(skinValue: String, skinSignature: String) {
        // npcProfile.properties.clear()
        try {
            npcProfile.properties.put(
                "textures",
                Property("textures", skinValue, skinSignature)
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        if (isSpawned()) {
            respawn(holders.toList())
        }
    }

    override fun despawn(players: List<Player>) {
        nmsWorld.removeEntity(npc)
        for (player in players) {
            holders.remove(player)
            hideFor(player)
        }
        PlayerNPCAPI.npcs.remove(getId())
    }

    override fun showFor(player: Player, onlyFlush: Boolean) {
        if (npcWorld != player.world) return
        val packetPlayerInfoAdd = PacketPlayOutPlayerInfo(
            PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
            npc
        )
        var packetPlayerSpawn: PacketPlayOutNamedEntitySpawn? = null
        var packetPlayerUpdateMetadata: PacketPlayOutEntityMetadata? = null
        var packetPlayerHeadRotation: PacketPlayOutEntityHeadRotation? = null
        if (!onlyFlush) {
             packetPlayerSpawn = PacketPlayOutNamedEntitySpawn(npc)
             packetPlayerUpdateMetadata = PacketPlayOutEntityMetadata(
                npc.id,
                npc.dataWatcher, true
            )
             packetPlayerHeadRotation = PacketPlayOutEntityHeadRotation(
                npc,
                MathHelper.d(npc.headRotation * 256.0f / 360.0f).toByte()
            )
        }
        val playerConnection = (player as CraftPlayer).handle.playerConnection
        playerConnection.sendPacket(packetPlayerInfoAdd)
        packetPlayerUpdateMetadata?.let { playerConnection.sendPacket(it) }
        packetPlayerSpawn?.let { playerConnection.sendPacket(it) }
        packetPlayerHeadRotation?.let { playerConnection.sendPacket(it) }

        val name = Extra.cutText("${0}NPC", 16) // Sets NPC tab's show priority to '0' (last)
        val team = Mine.getTeam(player.scoreboard ?: Mine.getMainScoreboard(), name)
        try {
            team.nameTagVisibility = NameTagVisibility.NEVER
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        team.prefix = "§8"
        if (!team.hasEntry(npc.name)) {
            team.addEntry(npc.name)
        }

        // This will remove the NPC name from the tab list after one second.
        // Shows the NPC in the tab is needed so clients will load the NPC skin.
        utilsMain.asyncDelay(20) {
            playerConnection.sendPacket(
                PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                    npc
                )
            )
        }
    }

    override fun hideFor(player: Player) {
        val packetDestroy = PacketPlayOutEntityDestroy(npc.id)
        val packetPlayerInfoRemove = PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc)
        val playerConnection = (player as CraftPlayer).handle.playerConnection
        playerConnection.sendPacket(packetDestroy)
        playerConnection.sendPacket(packetPlayerInfoRemove)
    }

    override fun updateFor(player: Player) {
        /*
        val packetPlayerUpdateMetadata = PacketPlayOutSpawnEntity(
            npc.id,
            npc.dataWatcher, true
        )
        player.mineSendPacket(packetPlayerUpdateMetadata)
         */
    }

    override fun respawn(players: List<Player>) {
        for (player in players.filter { p -> !PlayerNPCAPI.isNPC(p) }) {
            holders.add(player)
            hideFor(player)
            showFor(player, onlyFlush = false)
        }
    }

    override fun getSpawnLocation(): Location {
        return npcLocation
    }

    override fun setName(name: String) {
        this.npcName = name
        respawn(holders.toList())
    }

    override fun getId(): UUID {
        return npcId
    }
}