package com.mikael.mkutilslegacy.api.mkplugin.language

import com.mikael.mkutilslegacy.api.isProxyServer
import com.mikael.mkutilslegacy.bungee.api.utilsBungeeMain
import com.mikael.mkutilslegacy.spigot.api.utilsMain

@Suppress("WARNINGS")
object LangSystem {

    fun getText(translation: Translation): String {
        val mkPlugin = if (isProxyServer) utilsBungeeMain else utilsMain
        return when (mkPlugin.usingLanguage) {
            "en-us" -> {
                translation.en_us[0]
            }
            "pt-br" -> {
                translation.pt_br[0]
            }
            else -> {
                error("Cannot get Translation Text for language: ${mkPlugin.usingLanguage}")
            }
        }
    }

    fun getTextLines(translation: Translation): List<String> {
        val mkPlugin = if (isProxyServer) utilsBungeeMain else utilsMain
        return when (mkPlugin.usingLanguage) {
            "en-us" -> {
                translation.en_us
            }
            "pt-br" -> {
                translation.pt_br
            }
            else -> {
                error("Cannot get Translation Text for language: ${mkPlugin.usingLanguage}")
            }
        }
    }

}