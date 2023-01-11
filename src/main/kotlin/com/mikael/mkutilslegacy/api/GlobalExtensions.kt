package com.mikael.mkutilslegacy.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mikael.mkutilslegacy.api.mkplugin.MKPlugin
import com.mikael.mkutilslegacy.api.mkplugin.MKPluginData
import com.mikael.mkutilslegacy.api.redis.RedisAPI
import com.mikael.mkutilslegacy.bungee.api.utilsBungeeMain
import com.mikael.mkutilslegacy.spigot.api.utilsMain
import net.eduard.api.lib.hybrid.Hybrid
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * [UtilsManager] class shortcut.
 *
 * @see UtilsManager
 */
@Deprecated(
    "Call UtilsManager class instead.",
    ReplaceWith("UtilsManager", "com.mikael.mkutilslegacy.api.UtilsManager")
)
val Manager = UtilsManager

/**
 * [RedisAPI] class shortcut.
 *
 * @see RedisAPI
 */
@Deprecated(
    "Call RedisAPI class instead.",
    ReplaceWith("RedisAPI", "com.mikael.mkutilslegacy.api.redis.RedisAPI")
)
val Redis = RedisAPI

/**
 * Key to sync MySQL async and sync updates.
 * It's useful for plugins witch uses mkUtilsLegacy as dependency ([MKPlugin]).
 *
 * DO NOT USE IT BY YOURSELF IF YOU DO NOT KNOW WHAT YOU ARE DOING.
 *
 * @see syncMysql
 */
val syncMysqUpdatesKey = Any()

/**
 * Use it to sync updates in mysql that interact with a local list/map to save a [MKPluginData].
 * You can call this function in a main or async thread, everything will be sync as the same.
 *
 * @param thing the block code to execute using the [syncMysqUpdatesKey].
 * @return True if the block code has been executed with no error. Otherwise, false.
 * @author Mikael
 * @see syncMysqUpdatesKey
 */
@Deprecated("Deprecated; Use { synchronized(syncMysqUpdatesKey) { *code* } } instead.")
inline fun syncMysql(crossinline thing: (() -> Unit)): Boolean {
    synchronized(syncMysqUpdatesKey) {
        return try {
            thing.invoke()
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }
}

/**
 * @return True if the plugin is running on Bungeecord (Waterfall, etc). Otherwise, false.
 * @author Mikael
 */
val isProxyServer get() = Hybrid.instance.isBungeecord

/**
 * Transforms a [String]? into a [TextComponent].
 *
 * @param markNull if the given value is null and this is true, the text used will NOT be "", so "null" will be used.
 * @return [TextComponent] with the given [String], or empty if null is given.
 * @author Mikael
 * @see TextComponent
 */
fun String?.toTextComponent(markNull: Boolean = false): TextComponent {
    return if (this != null) {
        TextComponent(this)
    } else {
        TextComponent(if (markNull) "null" else  "")
    }
}

/**
 * You can use this to format something as yours (Personal).
 *
 * Example:
 *
 * * Notch.{[String.formatPersonal]} phone. -> Notch's phone.
 * * MKjubs.{[String.formatPersonal]} phone. -> MKjubs' phone.
 *
 * @return the new [String] as a Personal format.
 * @author Mikael
 */
fun String.formatPersonal(): String {
    return if (this.last() == 's') "${this}'" else "${this}'s"
}

/**
 * The given [String] will be grammar-fixed.
 *
 * Important: Characters considering append a net UpperCase lether at the moment is '.', '!' and '?'.
 * If the word doesn't end with one of them, the next letter will not be 'Upper-cased'.
 *
 * THIS FUNCTION WAS BUILT FOR THESE LANGUAGES: Brazilian Portuguese, Portuguese and US English.
 * This MAY work well with other languages, but was not tested for it.
 *
 * Example:
 *
 * - hi, how are you? -> Hi, how are you?
 * - hi, how are you? i'm fine, thanks. -> Hi, how are you? I'm fine, thanks.
 * - hey, Mikael! you here? -> Hey, Mikael! You here?
 *
 * @return the grammar-fixed [String].
 * @author Mikael
 */
fun String.fixGrammar(): String {
    val newTextBuilder = StringBuilder()
    for ((index, char) in this.toList().withIndex()) {
        if (index == 0 ||
            this.getOrNull((index - 2)) == '.' ||
            this.getOrNull((index - 2)) == '!' ||
            this.getOrNull((index - 2)) == '?'
        ) {
            newTextBuilder.append(char.uppercase())
            continue
        }
        newTextBuilder.append(char)
    }
    return newTextBuilder.toString()
}

/**
 * Will return a [String] with "seconds" if the given [Int] is different from 1. Otherwise, it will return "second".
 *
 * @return a [String] with "seconds" or "second". Can be '-1' if the given [Int] is negative (-1, -2, etc).
 * @author Mikael
 */
fun Int.formatSecondWorld(): String {
    if (this < 0) return "-1"
    return if (this != 1) return "seconds" else "second"
}

/**
 * @return a [String] with '§aEnabled' or '§cDisabled', following the given [Boolean].
 * @author Mikael
 */
fun Boolean.formatEnabledDisabled(colored: Boolean = true): String {
    val text = if (colored) {
        if (this) "§aEnabled" else "§cDisabled"
    } else {
        if (this) "Enabled" else "Disabled"
    }
    return text
}

/**
 * @return a [String] with '§aYes' or '§cNo', following the given [Boolean].
 * @author Mikael
 */
fun Boolean.formatYesNo(colored: Boolean = true): String {
    val text = if (colored) {
        if (this) "§aYes" else "§cNo"
    } else {
        if (this) "Yes" else "No"
    }
    return text
}

/**
 * @return a [String] with '§aON', '§cOFF' or the custom given texts, following the given [Boolean].
 * @author Mikael
 */
fun Boolean.formatOnOff(onText: String = "§aON", offText: String = "§cOFF"): String {
    return if (this) onText else offText
}

/**
 * @return True if the given [Int] is multiple of [multBy]. Otherwise, false.
 * @author Mikael
 */
fun Int.isMultOf(multBy: Int): Boolean {
    return this % multBy == 0
}

/**
 * @return True if the given [Double] is multiple of [multBy]. Otherwise, false.
 * @author Mikael
 */
fun Double.isMultOf(multBy: Double): Boolean {
    return this % multBy == 0.0
}

/**
 * Formats a [Number] using the North America (US) format.
 *
 * Example:
 *
 * * 1000 -> 1,000
 * * 1065 -> 1,065
 *
 * @return a [String] with the formatted value.
 * @author Mikael
 */
@Deprecated(
    "Use { Number.formatValue() } instead.", ReplaceWith(
        "NumberFormat.getNumberInstance(Locale.US).format(this)",
        "java.text.NumberFormat",
        "java.util.Locale"
    )
)
fun Number.formatEN(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)
}

/**
 * Formats a [Number] using the current [com.mikael.mkutilslegacy.api.mkplugin.MKPlugin.regionFormatter].
 *
 * Example ([Locale.US]):
 *
 * * 1000 -> 1,000
 * * 1065 -> 1,065
 * * 1000.5 -> 1,000.50
 *
 * @return a [String] with the formatted value.
 * @author Mikael
 */
fun Number.formatValue(): String {
    val mkPlugin = if (isProxyServer) utilsBungeeMain else utilsMain
    return NumberFormat.getNumberInstance(mkPlugin.regionFormatter).format(this)
}

/**
 * Formats a [Long] using the South America (BR) format.
 *
 * Examples of return:
 *
 * * 2d 10h 30m 30s
 * * 10d 5h 1m 3s
 *
 * Also, can return with some empty value. See:
 *
 * * 5d 10h 30m (seconds is not here because it's 0s)
 *
 * @return a formatted [String] with the duration. Can be '-1' if an invalid [Long] is given.
 * @author Mikael
 */
fun Long.formatDuration(): String {
    return if (this <= 0L) {
        "-1"
    } else {
        val day = TimeUnit.MILLISECONDS.toDays(this)
        val hours = TimeUnit.MILLISECONDS.toHours(this) - day * 24L
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.MILLISECONDS.toHours(this) * 60L
        val seconds = TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MILLISECONDS.toMinutes(this) * 60L
        val stringBuilder = StringBuilder()
        if (day > 0L) {
            stringBuilder.append(day).append("d")
            if (minutes > 0L || seconds > 0L || hours > 0L) {
                stringBuilder.append(" ")
            }
        }
        if (hours > 0L) {
            stringBuilder.append(hours).append("h")
            if (minutes > 0L || seconds > 0L) {
                stringBuilder.append(" ")
            }
        }
        if (minutes > 0L) {
            stringBuilder.append(minutes).append("m")
            if (seconds > 0L) {
                stringBuilder.append(" ")
            }
        }
        if (seconds > 0L) {
            stringBuilder.append(seconds).append("s")
        }
        val formatedTime = stringBuilder.toString()
        formatedTime.ifEmpty { "-1" }
    }
}

/**
 * Opens an [InputStreamReader] to build a [StringBuilder]. The returned value can be transformed to a JSON Object.
 *
 * @return the built [StringBuilder] with a 'possible' JSON Object.
 * @throws IOException
 * @author Mikael
 */
fun URL.stream(): String {
    this.openStream().use { input ->
        val isr = InputStreamReader(input)
        val reader = BufferedReader(isr)
        val json = java.lang.StringBuilder()
        var c: Int
        while (reader.read().also { c = it } != -1) {
            json.append(c.toChar())
        }
        return json.toString()
    }
}

/**
 * @return a [JsonObject] from the [URL].
 * @throws JsonIOException
 * @throws JsonSyntaxException
 * @author Mikael
 * @see URL.stream
 * @see JsonParser.parse
 */
fun URL.getJson(): JsonObject {
    return JsonParser().parse(this.stream()).asJsonObject
}

/**
 * @return The current server port ([Int]) running the given [MKPlugin].
 * @author Mikael
 */
val MKPlugin.serverPort: Int
    get() {
        return if (isProxyServer) {
            ProxyServer.getInstance().config.listeners.firstOrNull()?.queryPort
                ?: error("Cannot get ProxyServer query port")
        } else {
            Bukkit.getPort()
        }
    }

/**
 * @return a new [String] [List] with all given elements replaced.
 * @author Mikael
 * @see String.replace
 */
fun List<String>.replaceAll(oldValue: String, newValue: String, ignoreCase: Boolean = false): List<String> {
    val newList = mutableListOf<String>()
    this.forEach { line ->
        newList.add(line.replace(oldValue, newValue, ignoreCase))
    }
    return newList
}

/**
 * This function is more fast when comparing to [ChatColor.translateAlternateColorCodes] since this
 * uses the Kotlin String Replace and ignore the next chars after '&' and '§'.
 *
 * @return a new [String] replacing all '&' to '§'.
 * @author Mikael
 * @see String.replace
 */
fun String.mineColored(): String {
    return this.replace("&", "§")
}

fun List<String>.mineColored(): List<String> {
    return this.map { it.mineColored() }.toList()
}

/**
 * This will return the given [String] 'split' in lines, following the given [lineLength].
 *
 * @param lineLength the max length if each [String] that will be returned. Default: 50.
 * @return a new [List] of [String] with the lines broken using the given [lineLength].
 * @author Mikael
 * @author KoddyDev
 */
fun String.breakLines(lineLength: Int = 50): List<String> {
    val split = this.split(" ")
    val lines = mutableListOf<String>()
    for (word in split) {
        if (lines.isEmpty()) {
            lines.add(word)
            continue
        }
        val lastLine = lines.last()
        if (lastLine.length + word.length > lineLength) {
            lines.add(word)
        } else {
            lines[lines.lastIndex] = "$lastLine $word"
        }
    }
    return lines
}

/**
 * Please note that:
 * - 1.0 = 100% of chance
 * - 0.50 = 50% of chance
 * - 0.05 = 5% of chance
 *
 * @return True if [Math.random] <= [Double]. Otherwise, false.
 * @author Mikael
 * @see Math.random
 */
fun Double.getProbability(): Boolean {
    return Math.random() <= this
}

