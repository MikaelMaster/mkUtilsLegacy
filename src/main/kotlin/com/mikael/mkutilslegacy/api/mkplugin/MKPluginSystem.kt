package com.mikael.mkutilslegacy.api.mkplugin

import com.google.gson.JsonParser
import net.eduard.api.lib.modules.Extra
import java.net.URL
import java.nio.charset.StandardCharsets

object MKPluginSystem {

    val loadedMKPlugins = mutableListOf<MKPlugin>()
    val loadedPaidMKPlugins get() = loadedMKPlugins.first { !it.isFree } // MK Store

    init {
        loadedMKPlugins.clear()
    }

    /**
     * MK Store section.
     * This will be only used for paid proprietary MK plugins.
     *
     * The website '*api.mkstore.com*' must use this same version to correct conversation.
     *
     * @author Mikael
     * @see MKPlugin
     */
    const val storeAPICompatibleVersion = "v1.0.0"

    /**
     * Cached MK Store Customer Tokens.
     *
     * Format: MapOf<String, List<String>> -> CustomerToken, List(MKPluginName).
     *
     * The *List(MKPluginName)* is the owned plugins of that Customer Token.
     *
     * @author Mikael
     * @see requireActivation
     */
    @Deprecated("Invalid")
    private val cachedCustomerTokens = mutableMapOf<String, List<String>>()

    /**
     * This should be used to verify if a paid [MKPlugin] can be enabled with the given [customerToken].
     *
     * @param plugin the paid [MKPlugin] to check license.
     * @param customerToken the Customer Token to check if he has the access to the given [plugin].
     * @return True if the license is valid ([MKPluginStoreResponse.VALID]). Otherwise, false.
     * @throws IllegalStateException if the given [plugin] is not a paid [MKPlugin]. In other worlds, it's a [MKPlugin.isFree] (true) plugin.
     * @throws IllegalStateException if the given [customerToken] length is different from 16. Customer Tokens must always have 16 characters.
     * @author Mikael
     * @see MKPluginStoreResponse
     */
    fun requireActivation(plugin: MKPlugin, customerToken: String): Boolean {
        return try {
            plugin.log("§6[License] §eChecking Customer Token...")
            if (plugin.isFree) error("Cannot verify the license (token) of a free MK Plugin")
            if (customerToken.length != 16) error("customerToken must have 16 characters")

            // Request website (api.mkstore.com)
            val mkStoreAPIUrl = URL("https://")
            val connection = mkStoreAPIUrl.openConnection(); connection.useCaches = true
            val stream = connection.getInputStream()
            val json = Extra.readSTR(stream, StandardCharsets.UTF_8)
            val jsonObj = JsonParser().parse(json).asJsonObject

            // Verify website response (MKPluginStoreResponse)
            return when (val storeResponse = MKPluginStoreResponse.valueOf(jsonObj["response"].asString.uppercase())) {
                MKPluginStoreResponse.VALID -> {
                    plugin.log("§6[License] §aThe Customer Token is valid. Starting load...")
                    true
                }

                MKPluginStoreResponse.INVALID -> {
                    plugin.log("§6[License] §cThe Customer Token is not valid. Shutting down...")
                    false
                }

                MKPluginStoreResponse.RATE_LIMIT -> {
                    plugin.log("§6[License] §cCannot verify the Customer Token (RATE_LIMIT). Shutting down...")
                    false
                }

                MKPluginStoreResponse.API_ERROR -> {
                    plugin.log("§6[License] §cCannot verify the Customer Token (API_ERROR). Shutting down...")
                    false
                }

                else -> error("The returned response '${storeResponse.name}' is not valid. :c (is mkUtils up to date?)")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            plugin.log("§6[License] §cCannot verify the Customer Token (INTERNAL_ERROR). Shutting down...")
            false
        }
    }

    /**
     * @author Mikael
     * @see requireActivation
     */
    enum class MKPluginStoreResponse {
        VALID,
        INVALID,
        RATE_LIMIT,
        API_ERROR,
        INTERNAL_ERROR // mkUtils plugin-side only-- website (api.mkstore.com) will never return this
    }
}