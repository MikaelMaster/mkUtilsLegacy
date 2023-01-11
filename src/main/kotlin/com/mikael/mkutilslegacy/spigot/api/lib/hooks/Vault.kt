package com.mikael.mkutilslegacy.spigot.api.lib.hooks

import com.mikael.mkutilslegacy.spigot.UtilsMain
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * mkUtils Vault 'API'.
 *
 * @author KoddyDev
 * @author Mikael
 * @see Economy
 */
object Vault {

    /**
     * If it's hooked to the Vault Plugin.
     */
    private var isHooked = false

    /**
     * The [Economy].
     *
     * Lateinit; 'null'?.
     */
    private lateinit var economy: Economy

    /**
     * Runs on mkUtils onEnable.
     *
     * Internal.
     */
    internal fun onEnable() {
        isHooked = Bukkit.getPluginManager().isPluginEnabled("Vault")
        if (isHooked) {
            UtilsMain.instance.log("§6[VaultAPI] §aVault encontrado. Carregando VaultAPI...")
            economy = Bukkit.getServicesManager().getRegistration(Economy::class.java).provider
        } else {
            UtilsMain.instance.log("§6[VaultAPI] §cVault não encontrado. O VaultAPI não será carregado.")
        }
    }

    /**
     * Runs on mkUtils onDisable.
     *
     * Internal.
     */
    internal fun onDisable() {
        if (isHooked) {
            UtilsMain.instance.log("§6[VaultAPI] §eDescarregando VaultAPI...")
        }
        isHooked = false
    }

    // Vault 'API' methods - Start

    /**
     * @return True if the [Vault] is hooked to Vault Plugin. Otherwise, false.
     */
    fun isHooked(): Boolean {
        return isHooked
    }

    /**
     * Returns the given [Player] balance.
     *
     * @param player the player to get the balance.
     * @return the given player balance.
     * @throws IllegalStateException if it's not hooked to Vault Plugin ([isHooked]).
     */
    fun getPlayerBalance(player: Player): Double {
        if (!isHooked) error("VaultAPI isn't hooked!")
        return economy.getBalance(player)
    }

    /**
     * Sets the balance of the given [Player].
     *
     * @param player the player to add to its balance.
     * @param amount the new balance to set to the given player.
     * @return True if the action was completed with no errors. Otherwise, false.
     * @throws IllegalStateException if it's not hooked to Vault Plugin ([isHooked]).
     */
    fun setPlayerBalance(player: Player, amount: Double): Boolean {
        if (!isHooked) error("VaultAPI isn't hooked!")
        economy.withdrawPlayer(player, getPlayerBalance(player))
        economy.depositPlayer(player, amount)
        return true
    }

    /**
     * Adds to the balance of the given [Player].
     *
     * @param player the player to change add to its balance.
     * @param amount the value to add to the given player balance.
     * @return True if the action was completed with no errors. Otherwise, false.
     * @throws IllegalStateException if it's not hooked to Vault Plugin ([isHooked]).
     */
    fun addPlayerBalance(player: Player, amount: Double): Boolean {
        if (!isHooked) error("VaultAPI isn't hooked!")
        economy.depositPlayer(player, amount)
        return true
    }

    /**
     * Removes from the balance of the given [Player].
     *
     * @param player the player to remove from its balance.
     * @param amount the value to remove from the given player balance.
     * @return True if the action was completed with no errors. Otherwise, false.
     * @throws IllegalStateException if it's not hooked to Vault Plugin ([isHooked]).
     */
    fun removePlayerBalance(player: Player, amount: Double): Boolean {
        if (!isHooked) error("VaultAPI isn't hooked!")
        economy.withdrawPlayer(player, amount)
        return true
    }

    // Vault 'API' methods - End
}