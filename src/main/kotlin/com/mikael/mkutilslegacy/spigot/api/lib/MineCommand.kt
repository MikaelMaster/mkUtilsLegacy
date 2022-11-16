package com.mikael.mkutilslegacy.spigot.api.lib

import com.mikael.mkutilslegacy.api.mkplugin.MKPlugin
import com.mikael.mkutilslegacy.spigot.api.runBlock
import com.mikael.mkutilslegacy.spigot.api.soundNo
import net.eduard.api.lib.modules.Extra
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerCommandPreprocessEvent

/**
 * [MineCommand] util class
 *
 * *NOT FINISHED YET! DON'T USE IT!*
 *
 * This class represents a [CommandExecutor].
 *
 * To create a new MineCommand, extends it in a Class. As the example below:
 * - class TestCommand : MineCommand(command: [String]) { *class code* } -> This command will have no aliases.
 * - class TestCommand : MineCommand(command: [String], vararg aliasses: [String]) { *class code* } -> this command will have the given aliases.
 *
 * @param command the command name, as the example above.
 * @param aliases the command aliases, as the example above.
 * @author Mikael
 * @see CommandExecutor
 * @see command
 * @see playerCommand
 */
open class MineCommand(command: String, vararg aliases: String) : MineListener(), CommandExecutor {
    companion object {
        private val registeredMineCommands = mutableListOf<MineCommand>()

        fun getRegisteredMineCommands(): List<MineCommand> {
            return registeredMineCommands
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
     * The command usage. Mesage sent when a player use the command incorrectly.
     *
     * @see sendUsage
     */
    var usage: String = "/${command}"

    /**
     * The command 'name'.
     * Example: If the command is '/help' the plugin name will be 'help'.
     */
    val name = command

    /**
     * The command aliases.
     */
    val aliases = aliases.toList()

    /**
     * The command Subcommands.
     */
    private val subCommands = mutableListOf<MineCommand>()

    /**
     * Register this [MineCommand].
     *
     * @param plugin the plugin 'owner' of this command.
     */
    open fun registerCommand(plugin: MKPlugin) {
        registerListener(plugin)
        registeredMineCommands.add(this@MineCommand)
    }

    /**
     * Unregister this [MineCommand].
     */
    open fun unregisterCommand() {
        unregisterListener()
        registeredMineCommands.remove(this@MineCommand)
    }

    /**
     * Sends the Command [usage] to the [sender] (Player or Console) using a message.
     *
     * @see usage
     */
    fun sendUsage(sender: CommandSender) {
        sender.sendMessage(usage)
    }

    /**
     * This will run the command itself.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCommandProccess(e: PlayerCommandPreprocessEvent) {
        val player = e.player
        player.runBlock {
            val cmdName = e.message.split(" ")[0].replace("/", "")
            if (cmdName != name && !aliases.contains(cmdName)) return@runBlock

            executeCommand(player, Extra.getText(1, *e.message.split(" ").toTypedArray()).split(" "))
            e.isCancelled = true
        }
    }

    fun registerSubCommand(command: MineCommand) {
        subCommands.add(command)
    }

    /**
     * The code here will be executed when the [CommandSender] is a [Player].
     */
    open fun playerCommand(player: Player, args: List<String>) {
        player.sendMessage("§cOps! Something went wrong. :c")
    }

    /**
     * The code here will be executed when the [CommandSender] is NOT a [Player].
     *
     * IMPORTANT! In some cases if the command is hybrid this will be called for [Player]s too.
     */
    open fun command(sender: CommandSender, args: List<String>) {
        if (sender is Player) {
            playerCommand(sender, args)
        } else {
            sender.sendMessage("§cOnly players can use this command.")
        }
    }

    /**
     * The command code itself.
     *
     * @param sender the [CommandSender] that is trying to execute this command.
     * @param args the arguments given by the [sender] when executing the command.
     */
    private fun executeCommand(sender: CommandSender, args: List<String>) {
        try {
            if (permission != null && !sender.hasPermission(permission!!)) {
                if (sender is Player) {
                    sender.soundNo()
                }
                sender.sendMessage(permissionMessage)
                return
            }

            val subC = if(args.size > 1) args.last() else args[0]
            var sub: MineCommand? = null

            for (subCommand in subCommands) {
                if (subCommand.name.equals(subC, true)) {
                    sub = subCommand
                    break
                } else {
                    aliases@ for (alias in subCommand.aliases) {
                        if (alias.equals(subC, true)) {
                            sub = subCommand
                            break@aliases
                        }
                    }

                    if(sub == null) {
                        for (subSubCommand in subCommand.subCommands) {
                            if (subSubCommand.name.equals(subC, true)) {
                                sub = subSubCommand
                                break
                            } else {
                                aliases@ for (alias in subSubCommand.aliases) {
                                    if (alias.equals(subC, true)) {
                                        sub = subSubCommand
                                        break@aliases
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (sub != null) {
                val finallyArgs = args.toMutableList()
                finallyArgs.removeIf {
                    (it == sub.name) || (it in sub.aliases)
                }

                sub.command(sender, finallyArgs)
            } else {
                command(sender, args)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    // label = subcommand
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        executeCommand(sender, args?.toList() ?: listOf()) // The command code itself is here
        return true
    }

}