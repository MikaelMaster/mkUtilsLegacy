@file:Suppress("ClassName")

package com.mikael.mkutilslegacy.spigot.api.npc.npc_1_8_R3

import net.minecraft.server.v1_8_R3.EnumProtocolDirection
import net.minecraft.server.v1_8_R3.NetworkManager

/**
 * Network Manager for [PlayerNPC_1_8_R3].
 */
class NetworkManager_1_8_R3(enumProtocolDirection: EnumProtocolDirection?) :
    NetworkManager(enumProtocolDirection) {

    // I have no idea what this should do, but everything works with this marked as True.
    override fun g(): Boolean {
        return true
    }

    /*
        override fun a(i: Int) {
        }
        override fun c(): Boolean {
            return false
        }
        override fun l() {
        }
        override fun a() {
        }
        override fun a(
            packet: Packet<*>?,
            genericfuturelistener: GenericFutureListener<out Future<in Void>>?,
            vararg agenericfuturelistener: GenericFutureListener<out Future<in Void>>?
        ) {
        }
        override fun a(channelhandlercontext: ChannelHandlerContext?, packet: Packet<*>?) {
          }
        override fun a(secretkey: SecretKey?) {
        }
        override fun a(packetlistener: PacketListener?) {
        }
        override fun a(enumprotocol: EnumProtocol?) {
        }
        */
}