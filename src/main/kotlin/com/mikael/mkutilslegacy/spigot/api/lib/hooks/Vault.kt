package com.mikael.mkutilslegacy.spigot.api.lib.hooks

import com.mikael.mkutilslegacy.spigot.UtilsMain
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object Vault {
    private var isHooked = false
    private lateinit var economy: Economy

    fun onEnable() {
        isHooked = Bukkit.getPluginManager().isPluginEnabled("Vault")

        if (isHooked) {
            UtilsMain.instance.log("§e[VaultAPI] §aVault encontrado! Iniciando VaultAPI...")

            economy = Bukkit.getServicesManager().getRegistration(Economy::class.java).provider

        } else {
            UtilsMain.instance.log("§e[VaultAPI] §cVault não encontrado! VaultAPI desativado.")
        }
    }

    fun onDisable() {
        if(isHooked) {
            UtilsMain.instance.log("§e[VaultAPI] §cVaultAPI desativado.")
        }

        isHooked = false
    }

    fun isHooked() = isHooked

    fun getPlayerBalance(player: Player): Double {
        if (!isHooked) error("VaultAPI isn't hooked!")

        return economy.getBalance(player)
    }

    fun setPlayerBalance(player: Player, amount: Double) {
        if (!isHooked) error("VaultAPI isn't hooked!")

        economy.withdrawPlayer(player, getPlayerBalance(player))
        economy.depositPlayer(player, amount)
    }

    fun addPlayerBalance(player: Player, amount: Double) {
        if (!isHooked) error("VaultAPI isn't hooked!")

        economy.depositPlayer(player, amount)
    }

    fun removePlayerBalance(player: Player, amount: Double) {
        if (!isHooked) error("VaultAPI isn't hooked!")

        economy.withdrawPlayer(player, amount)
    }
}