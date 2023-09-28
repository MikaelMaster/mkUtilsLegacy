package com.mikael.mkutilslegacy.spigot.api.util.hooks

import com.mikael.mkutilslegacy.spigot.UtilsMain
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * mkUtils Extra for VaultAPI
 *
 * This is a simple 'Extra' for VaultAPI plugin to use Vault [Economy] methods in a simple way.
 *
 * @author KoddyDev
 * @author Mikael
 * @see Economy
 */
@Suppress("DEPRECATION")
object Vault {

    /**
     * If it's hooked to the Vault Plugin.
     *
     * This is set as True in [onEnable] if Vault plugin is founded. Otherwise, as False.
     */
    private var isHooked = false

    /**
     * The used [Economy].
     */
    private lateinit var economy: Economy

    // mkUtils onEnable
    internal fun onEnable() {
        isHooked = Bukkit.getPluginManager().isPluginEnabled("Vault")
        if (!isHooked) {
            UtilsMain.instance.log("§6[VaultAPI] §cVault not founded. VaultAPI will not be loaded.")
            return
        }
        UtilsMain.instance.log("§6[VaultAPI] §eVault founded. Loading VaultAPI...")
        economy = Bukkit.getServicesManager().getRegistration(Economy::class.java).provider
    }

    // mkUtils onDisable
    internal fun onDisable() {
        if (!isHooked) return
        UtilsMain.instance.log("§6[VaultAPI] §eUnloading VaultAPI...")
        isHooked = false
    }

    // Extra for VaultAPI methods - Start
    /**
     * @return True if the [Vault] is hooked to Vault plugin. Otherwise, false.
     */
    fun isHooked(): Boolean {
        return isHooked
    }

    /**
     * Returns the given [playerName] balance.
     *
     * @param playerName the player name to get the balance.
     * @return the given [playerName] balance.
     * @throws IllegalStateException if it's not hooked to Vault Plugin ([isHooked]).
     */
    fun getPlayerBalance(playerName: String): Double {
        if (!isHooked) error("VaultAPI isn't hooked!")
        return economy.getBalance(playerName)
    }

    /**
     * Returns the given [player] balance.
     *
     * @param player the player to get the balance.
     * @return the given [player] balance.
     * @throws IllegalStateException if it's not hooked to Vault Plugin ([isHooked]).
     */
    fun getPlayerBalance(player: Player): Double {
        return getPlayerBalance(player.name)
    }

    /**
     * Sets the balance of the given [playerName] to the given [amount].
     *
     * @param playerName the player name to add to its balance.
     * @param amount the new balance to set to the given player name.
     * @return True if the action was completed with no errors. Otherwise, false.
     * @throws IllegalStateException if it's not hooked to Vault Plugin ([isHooked]).
     */
    fun setPlayerBalance(playerName: String, amount: Double): Boolean {
        if (!isHooked) error("VaultAPI isn't hooked!")
        if (amount < 0) error("Amount cannot be less than 0; Given amount: $amount")
        economy.withdrawPlayer(playerName, getPlayerBalance(playerName))
        economy.depositPlayer(playerName, amount)
        return true
    }

    /**
     * Sets the balance of the given [player] to the given [amount].
     *
     * @param player the player to add to its balance.
     * @param amount the new balance to set to the given player.
     * @return True if the action was completed with no errors. Otherwise, false.
     * @throws IllegalStateException if it's not hooked to Vault Plugin ([isHooked]).
     */
    fun setPlayerBalance(player: Player, amount: Double): Boolean {
        return setPlayerBalance(player.name, amount)
    }

    /**
     * Adds to the balance of the given [playerName] the given [amount].
     *
     * @param playerName the player name to add the amount to his balance.
     * @param amount the value to add to the given [playerName] balance.
     * @return True if the action was completed with no errors. Otherwise, false.
     * @throws IllegalStateException if it's not hooked to Vault Plugin ([isHooked]).
     */
    fun addPlayerBalance(playerName: String, amount: Double): Boolean {
        if (!isHooked) error("VaultAPI isn't hooked!")
        if (amount < 0) error("Amount cannot be less than 0; Given amount: $amount")
        economy.depositPlayer(playerName, amount)
        return true
    }

    /**
     * Adds to the balance of the given [player] the given [amount].
     *
     * @param player the player name to add the amount to his balance.
     * @param amount the value to add to the given [player] balance.
     * @return True if the action was completed with no errors. Otherwise, false.
     * @throws IllegalStateException if it's not hooked to Vault Plugin ([isHooked]).
     */
    fun addPlayerBalance(player: Player, amount: Double): Boolean {
        return addPlayerBalance(player.name, amount)
    }

    /**
     * Removes from the balance of the given [playerName] the given [amount].
     *
     * @param playerName the player name to remove from his balance.
     * @param amount the value to remove from the given [playerName] balance.
     * @return True if the action was completed with no errors. Otherwise, false.
     * @throws IllegalStateException if it's not hooked to Vault Plugin ([isHooked]).
     */
    fun removePlayerBalance(playerName: String, amount: Double): Boolean {
        if (!isHooked) error("VaultAPI isn't hooked!")
        if (amount < 0) error("Amount cannot be less than 0; Given amount: $amount")
        economy.withdrawPlayer(playerName, amount)
        return true
    }

    /**
     * Removes from the balance of the given [player] the given [amount].
     *
     * @param player the player name to remove from his balance.
     * @param amount the value to remove from the given [player] balance.
     * @return True if the action was completed with no errors. Otherwise, false.
     * @throws IllegalStateException if it's not hooked to Vault Plugin ([isHooked]).
     */
    fun removePlayerBalance(player: Player, amount: Double): Boolean {
        return removePlayerBalance(player.name, amount)
    }
    // Extra for VaultAPI methods - End

}