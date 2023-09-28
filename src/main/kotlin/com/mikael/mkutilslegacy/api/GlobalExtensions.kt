@file:Suppress("WARNINGS")

package com.mikael.mkutilslegacy.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mikael.mkutilslegacy.api.mkplugin.MKPlugin
import com.mikael.mkutilslegacy.bungee.api.utilsBungeeMain
import com.mikael.mkutilslegacy.spigot.api.utilsMain
import net.eduard.api.lib.hybrid.Hybrid
import net.eduard.api.lib.kotlin.fixColors
import net.eduard.api.lib.modules.Extra
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.json.JSONObject
import java.io.*
import java.net.URL
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream

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
 * @author KoddyDev
 * @see TextComponent
 */
fun String?.toTextComponent(fixColors: Boolean = true, markNull: Boolean = false): TextComponent {
    val textComponent: TextComponent = if (this != null) {
        TextComponent(this)
    } else {
        TextComponent(if (markNull) "null" else "")
    }

    return if (fixColors) textComponent.fixColors() as TextComponent else textComponent
}

/**
 * Formats the given [String] into a money ([Double]) format.
 *
 * @author Eduard
 * @author KoddyDev
 * @see Extra.fromMoneyToDouble
 */
fun String.getMoneyValue(): Double {
    return Extra.fromMoneyToDouble(this)
}

/**
 * You can use this to format something as yours (Personal).
 *
 * THIS FUNCTION WAS BUILT FOR English US language and may not make sense in others languages.
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

fun String.compress(): String {
    val str = this
    val out = ByteArrayOutputStream()
    val gzip = GZIPOutputStream(out)
    gzip.write(str.toByteArray())
    gzip.close()
    return out.toString("UTF-8")
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
        if (index == 0 || this.getOrNull((index - 2)) == '.' || this.getOrNull((index - 2)) == '!' || this.getOrNull((index - 2)) == '?') {
            newTextBuilder.append(char.uppercase())
            continue
        }
        newTextBuilder.append(char)
    }
    return newTextBuilder.toString()
}

public var FORMAT_SECOND_WORLD_TEXT_SINGLE = "second"
public var FORMAT_SECOND_WORLD_TEXT_MULTI = "seconds"

/**
 * Will return a [String] with '[FORMAT_SECOND_WORLD_TEXT_MULTI]' if the given [Int] is different from 1. Otherwise, it will return '[FORMAT_SECOND_WORLD_TEXT_SINGLE]'.
 *
 * @return a [String] with "seconds" or "second". Can be '-1' if the given [Int] is negative (-1, -2, etc).
 * @author Mikael
 */
fun Int.formatSecondWorld(): String {
    if (this < 0) return "-1"
    return if (this == 1) return FORMAT_SECOND_WORLD_TEXT_SINGLE else FORMAT_SECOND_WORLD_TEXT_MULTI
}

public var FORMAT_ENABLED_DISABLED_TEXT_ENABLED = "Enabled"
public var FORMAT_ENABLED_DISABLED_TEXT_DISABLED = "Disabled"

/**
 * @return a [String] with '[FORMAT_ENABLED_DISABLED_TEXT_ENABLED]' or '[FORMAT_ENABLED_DISABLED_TEXT_DISABLED]', following the given [Boolean].
 * @author Mikael
 */
fun Boolean.formatEnabledDisabled(colored: Boolean = true): String {
    val text = if (colored) {
        if (this) "§a${FORMAT_ENABLED_DISABLED_TEXT_ENABLED}" else "§c${FORMAT_ENABLED_DISABLED_TEXT_DISABLED}"
    } else {
        if (this) FORMAT_ENABLED_DISABLED_TEXT_ENABLED else FORMAT_ENABLED_DISABLED_TEXT_DISABLED
    }
    return text
}

public var FORMAT_YES_NO_TEXT_YES = "Yes"
public var FORMAT_YES_NO_TEXT_NO = "No"

/**
 * @return a [String] with '[FORMAT_YES_NO_TEXT_YES]' or '[FORMAT_YES_NO_TEXT_NO]', following the given [Boolean].
 * @author Mikael
 */
fun Boolean.formatYesNo(colored: Boolean = true): String {
    val text = if (colored) {
        if (this) "§a${FORMAT_YES_NO_TEXT_YES}" else "§c${FORMAT_YES_NO_TEXT_NO}"
    } else {
        if (this) FORMAT_YES_NO_TEXT_YES else FORMAT_YES_NO_TEXT_NO
    }
    return text
}

public var FORMAT_ON_OFF_TEXT_ON = "ON"
public var FORMAT_ON_OFF_TEXT_OFF = "OFF"

/**
 * @return a [String] with '[FORMAT_ON_OFF_TEXT_ON]' or '[FORMAT_ON_OFF_TEXT_OFF]' following the given [Boolean].
 * @author Mikael
 */
fun Boolean.formatOnOff(colored: Boolean = true): String {
    val text = if (colored) {
        if (this) "§a${FORMAT_ON_OFF_TEXT_ON}" else "§c${FORMAT_ON_OFF_TEXT_OFF}"
    } else {
        if (this) FORMAT_ON_OFF_TEXT_ON else FORMAT_ON_OFF_TEXT_OFF
    }
    return text
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
 * @see URL.readText
 */
fun URL.stream(): String {
    return this.readText()
}

/**
 * @see URL.getJson
 */
private val jsonParser = JsonParser()

/**
 * Returns a built [JsonObject] based on the returned response from the given [URL].
 *
 * @return A [JsonObject] from the given [URL].
 * @throws JsonIOException
 * @throws JsonSyntaxException
 * @author Mikael
 * @see URL.stream
 * @see jsonParser
 */
fun URL.getJson(): JsonObject {
    return jsonParser.parse(this.stream()).asJsonObject
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
 * @return A new Lis([String]) with all given elements replaced.
 * @author Mikael
 * @see String.replace
 */
fun List<String>.replaceAll(oldValue: String, newValue: String, ignoreCase: Boolean = false): List<String> {
    return this.map { it.replace(oldValue, newValue, ignoreCase) }
}

/**
 * This function is more fast when comparing to [ChatColor.translateAlternateColorCodes] since this
 * uses the Kotlin String Replace and ignore the next chars after '&' and '§'.
 *
 * @param justColors if false, others codes like '&l' (bold) will be translated. Default: True.
 * @return a new [String] replacing all '&' to '§'.
 * @author Mikael
 * @see String.replace
 */
fun String.mineColored(justColors: Boolean = true): String {
    return if (!justColors) {
        this.replace("&", "§")
    } else {
        this.replace("&", "§")
            .replace("§k", "", true)
            .replace("§l", "", true)
            .replace("§m", "", true)
            .replace("§n", "", true)
            .replace("§o", "", true)
            .replace("§r", "", true)
    }
}

/**
 * @author KoddyDev
 * @author Mikael
 * @see String.mineColored
 */
fun List<String>.mineColored(justColors: Boolean = true): List<String> {
    return this.map { it.mineColored(justColors) }
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
        val lastLine = lines.lastOrNull()
        if (lastLine == null) {
            lines.add(word)
            continue
        }
        if (lastLine.length + word.length >= lineLength) {
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

/**
 * @return A value for the [key] or null if the key does not exists on the given [JSONObject].
 */
fun JSONObject.getOrNull(key: String): Any? {
    if (!this.has(key)) return null
    return this.get(key)
}

/**
 * Runs the given [thing] using try catch.
 * If an error occur, it'll be printed into console and false will be returned.
 *
 * @param thing the block code to run using try catch
 * @return True if the given [thing] was run with no errors. Otherwise, false.
 * @throws Exception as the possibl error itself.
 */
fun runTryCatch(thing: (() -> Unit)): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        false
    }
}

/**
 * Serializes the given [Serializable] object into a [ByteArray].
 *
 * @throws IOException
 * @author KoddyDev
 */
fun Serializable.toByteArray(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
    objectOutputStream.writeObject(this)
    objectOutputStream.close()
    return byteArrayOutputStream.toByteArray()
}

/**
 * Deserializes the given [byteArray] to the given [T] from the [Serializable] object.
 *
 * @throws IOException
 * @throws ClassNotFoundException
 * @author KoddyDev
 */
@Throws(IOException::class, ClassNotFoundException::class)
fun <T : Serializable> fromByteArray(byteArray: ByteArray): T {
    return ByteArrayInputStream(byteArray).use { byteArrayInputStream ->
        ObjectInputStream(byteArrayInputStream).use { objectInput ->
            objectInput.readObject() as T
        }
    }
}

// Used by 'Player.clearChat()' and 'ProxiedPlayer.clearChat()'.
internal val chatClear = mutableListOf<String>().apply {
    repeat(150) {
        this.add("§r")
    }
}.toTypedArray()