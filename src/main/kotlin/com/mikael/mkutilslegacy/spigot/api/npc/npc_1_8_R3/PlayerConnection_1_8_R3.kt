@file:Suppress("ClassName")

package com.mikael.mkutilslegacy.spigot.api.npc.npc_1_8_R3

import net.eduard.api.lib.modules.Mine
import net.minecraft.server.v1_8_R3.EntityPlayer
import net.minecraft.server.v1_8_R3.EnumProtocolDirection
import net.minecraft.server.v1_8_R3.Packet
import net.minecraft.server.v1_8_R3.PlayerConnection

/**
 * Player Connection for [PlayerNPC_1_8_R3].
 */
class PlayerConnection_1_8_R3(entityplayer: EntityPlayer?) : PlayerConnection(
    entityplayer?.server,
    NetworkManager_1_8_R3(EnumProtocolDirection.CLIENTBOUND),
    entityplayer
) {
    override fun sendPacket(packet: Packet<*>?) {

    }

    var disconnected = false

    override fun isDisconnected(): Boolean {
        return disconnected
    }

    override fun disconnect(reason: String?) {
        // disconnected = true
        Mine.console("§cDisconnecting fake player (NPC): §e$reason")
    }
    /*
        override fun c() {
        }
        override fun a(packetplayinsteervehicle: PacketPlayInSteerVehicle?) {
        }
        override fun a(packetplayinflying: PacketPlayInFlying?) {
        }
        override fun a(packetplayinblockdig: PacketPlayInBlockDig?) {
        }
        override fun chat(s: String?, async: Boolean) {
        }
        override fun a(
            d0: Double,
            d1: Double,
            d2: Double,
            f: Float,
            f1: Float,
            set: MutableSet<PacketPlayOutPosition.EnumPlayerTeleportFlags>?
        ) {
        }
        override fun a(d0: Double, d1: Double, d2: Double, f: Float, f1: Float) {
        }
        override fun a(packetplayinwindowclick: PacketPlayInWindowClick?) {
        }
        override fun a(packetplayinuseentity: PacketPlayInUseEntity?) {
        }
        override fun a(ichatbasecomponent: IChatBaseComponent?) {
        }
        override fun a(packetplayinupdatesign: PacketPlayInUpdateSign?) {
        }
        override fun a(packetplayintransaction: PacketPlayInTransaction?) {
        }
        override fun a(packetplayintabcomplete: PacketPlayInTabComplete?) {
        }
        override fun a(packetplayinspectate: PacketPlayInSpectate?) {
        }
        override fun a(packetplayinsettings: PacketPlayInSettings?) {
        }
        override fun a(packetplayinabilities: PacketPlayInAbilities?) {
        }
        override fun a(packetplayinarmanimation: PacketPlayInArmAnimation?) {
        }
        override fun a(packetplayinblockplace: PacketPlayInBlockPlace?) {
        }
        override fun a(packetplayinchat: PacketPlayInChat?) {
        }
        override fun a(packetplayinclientcommand: PacketPlayInClientCommand?) {
        }
        override fun a(packetplayinclosewindow: PacketPlayInCloseWindow?) {
        }
        override fun a(packetplayincustompayload: PacketPlayInCustomPayload?) {
        }
        override fun a(packetplayinenchantitem: PacketPlayInEnchantItem?) {
        }
        override fun a(packetplayinentityaction: PacketPlayInEntityAction?) {
        }
        override fun a(packetplayinhelditemslot: PacketPlayInHeldItemSlot?) {
        }
        override fun a(packetplayinkeepalive: PacketPlayInKeepAlive?) {
        }
        override fun a(packetplayinresourcepackstatus: PacketPlayInResourcePackStatus?) {
        }
        override fun a(packetplayinsetcreativeslot: PacketPlayInSetCreativeSlot?) {
        }
        override fun teleport(dest: Location?) {
        }
        */
}