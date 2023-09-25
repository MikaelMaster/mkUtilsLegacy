@file:Suppress("WARNINGS")

package com.mikael.mkutilslegacy.bungee.api

import com.mikael.mkutilslegacy.api.chatClear
import com.mikael.mkutilslegacy.api.toTextComponent
import com.mikael.mkutilslegacy.bungee.UtilsBungeeMain
import net.eduard.api.lib.hybrid.ISender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer

/**
 * Shortcut to get the [UtilsBungeeMain.instance].
 *
 * @return the [UtilsBungeeMain.instance].
 */
val utilsBungeeMain get() = UtilsBungeeMain.instance

public var PROXY_RUN_COMMAND_ERROR_MSG = "§c[Proxy] An internal error occurred while executing this command."

/**
 * Use in a proxy command to run the Unit using a try catch. If any error occur,
 * the given [ISender] (can be the Console) will receive a message saying that an error occurred.
 *
 * Uses [PROXY_RUN_COMMAND_ERROR_MSG] to send the player a message if an error occur.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was run with no errors. Otherwise, false.
 */
inline fun ISender.runCommand(
    crossinline thing: (() -> Unit)): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.sendMessage(PROXY_RUN_COMMAND_ERROR_MSG)
        false
    }
}

/**
 * Use in a proxy command to run the Unit using a try catch. If any error occur,
 * the given [ProxiedPlayer] will receive a message saying that an error occurred.
 *
 * Uses [PROXY_RUN_COMMAND_ERROR_MSG] to send the player a message if an error occur.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was run with no errors. Otherwise, false.
 */
inline fun ProxiedPlayer.runCommand(crossinline thing: (() -> Unit)): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.sendMessage(PROXY_RUN_COMMAND_ERROR_MSG.toTextComponent())
        false
    }
}

public var PROXY_PLAYER_RUN_BLOCK_ERROR_MSG = "§c[Proxy] An internal error occurred while executing something to you."

/**
 * Use it anywhere to run the Unit using a try catch. If any error occur,
 * the given [ProxiedPlayer] will receive a message saying that an error occurred.
 *
 * Uses [PROXY_PLAYER_RUN_BLOCK_ERROR_MSG] to send the player a message if an error occur.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was run with no errors. Otherwise, false.
 */
inline fun ProxiedPlayer.runBlock(crossinline thing: (() -> Unit)): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.sendMessage(PROXY_PLAYER_RUN_BLOCK_ERROR_MSG.toTextComponent())
        false
    }
}

/**
 * Sends a [title] and a [subtitle] to the given [ProxiedPlayer].
 *
 * @see ProxyServer.createTitle
 * @see ProxiedPlayer.sendTitle
 */
fun ProxiedPlayer.title(title: String?, subtitle: String?, fadeIn: Int = 10, stay: Int = 20 * 2, fadeOut: Int = 10) {
    val proxyTitle = ProxyServer.getInstance().createTitle()
    proxyTitle.reset()
    proxyTitle.title(title.toTextComponent())
    proxyTitle.title(subtitle.toTextComponent())
    proxyTitle.fadeIn(fadeIn)
    proxyTitle.stay(stay)
    proxyTitle.fadeOut(fadeOut)
    this.sendTitle(proxyTitle)
}

/**
 * Clears the given [ProxiedPlayer] client title field.
 */
fun ProxiedPlayer.clearTitle() {
    this.sendTitle(ProxyServer.getInstance().createTitle().reset())
}

/**
 * Sends all the [messages] to the given [ProxiedPlayer].
 *
 * @see ProxiedPlayer.sendMessages
 */
@Deprecated("You should no use 'ProxiedPlayer.sendMessages(...)' instead.")
fun ProxiedPlayer.sendMessage(vararg messages: String) {
    this.sendMessages(*messages)
}

/**
 * Sends all the [messages] to the given [ProxiedPlayer].
 */
fun ProxiedPlayer.sendMessages(vararg messages: String) {
    val messagesToSend = messages.map { it.toTextComponent() }.toTypedArray()
    this.sendMessage(*messagesToSend)
}

/**
 * Clear the chat of the given [ProxiedPlayer] by sending 150 messages.
 *
 * IMPORTANT: If the given [ProxiedPlayer]'s client have infinity chat scrolling,
 * this will be more or less useless.
 *
 * @see chatClear
 */
fun ProxiedPlayer.clearChat() {
    this.sendMessage(*chatClear)
}