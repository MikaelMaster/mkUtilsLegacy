package com.mikael.mkutilslegacy.bungee.api.lib

import com.mikael.mkutilslegacy.api.mkplugin.MKPlugin
import com.mikael.mkutilslegacy.api.toTextComponent
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.TabExecutor
import org.bukkit.entity.Player

/**
 * [ProxyCommand] util class
 *
 * This class represents a [Command] and extends a [TabExecutor].
 *
 * To create a new ProxyCommand, extends it in a Class. As the example below:
 * - class TestCommand : ProxyCommand(command: [String]) { *class code* } -> This command will have no aliases.
 * - class TestCommand : ProxyCommand(command: [String], vararg aliases: [String]) { *class code* } -> This command will have the given aliases.
 *
 * @param command the command name, as the example above.
 * @param aliases the command aliases, as the example above.
 * @author KoddyDev
 * @author Mikael
 * @see Command
 */
@Suppress("WARNINGS")
open class ProxyCommand(command: String, vararg aliases: String) : TabExecutor {
    companion object {
        private val registeredProxyCommands = mutableListOf<ProxyCommand>()

        /**
         * @return all registered [ProxyCommand]s.
         * @see registeredProxyCommands
         */
        fun getRegisteredProxyCommands(): List<ProxyCommand> {
            return registeredProxyCommands
        }
    }

    /**
     * The command permission.
     * If null, everyone will be able to use this command.
     */
    var permission: String? = null

    /**
     * The command permission message.
     * Message sent when a player don't have permission to use it.
     */
    var permissionMessage: String = "§cYou don't have permission to use this command."

    /**
     * The command usage. Message sent when a player use the command incorrectly.
     *
     * @see sendUsage
     */
    var usage: String = "/${command}"

    /**
     * The command 'name'.
     * Example: If the command is '/help' the plugin name will be 'help'.
     */
    val name = command

    lateinit var plugin: Plugin

    /**
     * The command aliases.
     */
    val aliases = aliases.toList()

    /**
     * The command Subcommands.
     */
    private val subCommands = mutableListOf<ProxyCommand>()

    private lateinit var proxyCommand: SimpleProxyCommand

    /**
     * Register this [ProxyCommand].
     *
     * @param mkPlugin the plugin 'owner' of this command.
     */
    open fun registerCommand(mkPlugin: MKPlugin) {
        plugin = mkPlugin as Plugin
        proxyCommand = SimpleProxyCommand(this@ProxyCommand)
        plugin.proxy.pluginManager.registerCommand(plugin, proxyCommand)
        registeredProxyCommands.add(this@ProxyCommand)
    }

    /**
     * Unregisters this [ProxyCommand].
     */
    open fun unregisterCommand() {
        plugin.proxy.pluginManager.unregisterCommand(proxyCommand)

        registeredProxyCommands.remove(this@ProxyCommand)
    }

    /**
     * Sends the Command [usage] to the [sender] (Player or Console) using a message.
     *
     * @see usage
     */
    fun sendUsage(sender: CommandSender) {
        sender.sendMessage("§cUse: $usage".toTextComponent())
    }

    /**
     * Registers the given [subCommand] as child of this [ProxyCommand].
     */
    fun registerSubCommand(subCommand: ProxyCommand) {
        subCommands.add(subCommand)
    }

    /**
     * The code here will be executed when the [CommandSender] is a [Player].
     */
    open fun playerCommand(player: ProxiedPlayer, args: List<String>) {
        player.sendMessage("§cOps! Something went wrong. :c".toTextComponent())
    }

    /**
     * The code here will be executed when the [CommandSender] is NOT a [Player].
     *
     * IMPORTANT! In some cases if the command is hybrid this will be called for [Player]s too.
     */
    open fun command(sender: CommandSender, args: List<String>) {
        if (sender is ProxiedPlayer) {
            playerCommand(sender, args)
        } else {
            sender.sendMessage("§cOnly players can use this command.".toTextComponent())
        }
    }

    /**
     * The command code itself.
     *
     * @param sender the [CommandSender] that is trying to execute this command.
     * @param args the arguments given by the [sender] when executing the command.
     */
    internal fun executeCommand(sender: CommandSender, args: List<String>) {
        try {
            val subC = args.firstOrNull()?.lowercase()
            var sub: ProxyCommand? = null

            for (subCommand in subCommands) {
                if (subCommand.name.lowercase() == subC) {
                    sub = subCommand
                    break
                } else {
                    aliases@ for (alias in subCommand.aliases) {
                        if (alias.lowercase() == subC) {
                            sub = subCommand
                            break@aliases
                        }
                    }
                }
            }

            if (sub != null) {
                val finallyArgs = args.toMutableList()
                finallyArgs.removeIf {
                    (it == sub.name) || (it in sub.aliases)
                }
                val subPerm = sub.permission
                if (subPerm != null && !sender.hasPermission(subPerm)) {
                    sender.sendMessage(sub.permissionMessage.toTextComponent())
                    return
                }

                sub.command(sender, finallyArgs)
            } else {
                val commandPerm = permission
                if (commandPerm != null && !sender.hasPermission(commandPerm)) {
                    sender.sendMessage(permissionMessage.toTextComponent())
                    return
                }
                command(sender, args)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): MutableIterable<String> {
        val data = mutableListOf<String>()

        if (sender !is ProxiedPlayer) return mutableListOf()
        if (args == null || args.isEmpty()) {
            data.addAll(subCommands.map { it.name})

            data.addAll(
                plugin.proxy.players.filter { it.server == sender.server }.map { it.name }
            )
        } else {
            data.addAll(subCommands.map { it.name}.filter { it.lowercase().startsWith(args.last().lowercase()) })

            data.addAll(
                plugin.proxy.players.filter { it.server == sender.server }.map { it.name }
                    .filter { it.lowercase().startsWith(args[0]) }
            )
        }

        return data
    }

    class SimpleProxyCommand(private val proxyCommand: ProxyCommand) :
        Command(proxyCommand.name, null, *proxyCommand.aliases.toTypedArray()) {

        override fun execute(sender: CommandSender?, args: Array<out String>?) {
            proxyCommand.executeCommand(sender!!, args?.toList() ?: emptyList())
        }
    }

}