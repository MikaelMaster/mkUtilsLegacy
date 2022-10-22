package com.mikael.mkutilslegacy.bungee.api

import com.mikael.mkutilslegacy.api.toTextComponent
import net.eduard.api.lib.hybrid.ISender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer

/**
 * Use in a proxy command to run the Unit using a try catch. If any error occur,
 * the given [ISender] (can be the Console) will receive a message saying that an error occurred.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was run with no errors. Otherwise, false.
 */
inline fun ISender.runCommand(
    errorMessage: String = "§c[Proxy] An internal error occurred while executing this command.",
    crossinline thing: (() -> Unit)
): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.sendMessage(errorMessage)
        false
    }
}

/**
 * Use in a proxy command to run the Unit using a try catch. If any error occur,
 * the given [ProxiedPlayer] will receive a message saying that an error occurred.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was run with no errors. Otherwise, false.
 */
inline fun ProxiedPlayer.runCommand(
    errorMessage: String = "§c[Proxy] An internal error occurred while executing this command.",
    crossinline thing: (() -> Unit)
): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.sendMessage(errorMessage.toTextComponent())
        false
    }
}

/**
 * Use it anywhere to run the Unit using a try catch. If any error occur,
 * the given [ProxiedPlayer] will receive a message saying that an error occurred.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was run with no errors. Otherwise, false.
 */
inline fun ProxiedPlayer.runBlock(
    errorMessage: String = "§c[Proxy] An internal error occurred while executing something to you.",
    crossinline thing: (() -> Unit)
): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.sendMessage(errorMessage.toTextComponent())
        false
    }
}

/**
 * Sends a [title] and [subtitle] to the given [ProxiedPlayer].
 *
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
