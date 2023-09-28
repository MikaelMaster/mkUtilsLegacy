package com.mikael.mkutilslegacy.spigot.api.lib

import com.mikael.mkutilslegacy.api.mkplugin.MKPlugin
import com.mikael.mkutilslegacy.spigot.UtilsMain
import com.mikael.mkutilslegacy.spigot.api.runBlock
import com.mikael.mkutilslegacy.spigot.api.soundNo
import net.eduard.api.lib.modules.Extra
import net.eduard.api.lib.modules.Mine
import org.bukkit.command.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.plugin.java.JavaPlugin

/**
 * [MineCommand] util class
 *
 * This class represents a [CommandExecutor].
 *
 * To create a new MineCommand, extends it in a Class. As the example below:
 * - class TestCommand : MineCommand(command: [String]) { *class code* } -> This command will have no aliases.
 * - class TestCommand : MineCommand(command: [String], vararg aliases: [String]) { *class code* } -> This command will have the given aliases.
 *
 * @param command the command name, as the example above.
 * @param aliases the command aliases, as the example above.
 * @author Mikael
 * @author KoddyDev
 * @see CommandExecutor
 * @see command
 * @see playerCommand
 */
open class MineCommand(command: String, vararg aliases: String) : MineListener(), CommandExecutor, TabExecutor {
    companion object {
        private val registeredMineCommands = mutableListOf<MineCommand>()

        /**
         * @return all registered [MineCommand]s.
         * @see registeredMineCommands
         */
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

    lateinit var plugin: JavaPlugin

    /**
     * The command aliases.
     */
    val aliases = aliases.toList()

    /**
     * The command Subcommands.
     */
    private val subCommands = mutableListOf<MineCommand>()

    internal lateinit var bukkitCommand: SimpleCommand

    /**
     * Register this [MineCommand].
     *
     * @param plugin the plugin 'owner' of this command.
     */
    open fun registerCommand(plugin: MKPlugin) {
        this.plugin = plugin as JavaPlugin
        bukkitCommand = SimpleCommand(this)
        UtilsMain.instance.scm!!.register(name, bukkitCommand)
        registeredMineCommands.add(this@MineCommand)
        this@MineCommand.registerListener(plugin)
    }

    /**
     * Unregisters this [MineCommand].
     */
    open fun unregisterCommand() {
        this@MineCommand.unregisterListener()
        bukkitCommand.unregister(UtilsMain.instance.scm)
        registeredMineCommands.remove(this@MineCommand)
    }

    /**
     * Sends the Command [usage] to the [sender] (Player or Console) using a message.
     *
     * @see usage
     */
    fun sendUsage(sender: CommandSender) {
        sender.sendMessage("§cUse: $usage")
    }

    /**
     * This will run the command itself.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCommandPreProcess(e: PlayerCommandPreprocessEvent) {
        val player = e.player
        player.runBlock {
            val cmdName = e.message.split(" ")[0].replace("/", "")
            if (cmdName != name && !aliases.contains(cmdName)) return@runBlock
            try {
                val args = Extra.getText(1, *e.message.split(" ").toTypedArray()).split(" ").filter { it.isNotEmpty() }
                executeCommand(player, args.ifEmpty { emptyList() })
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            e.isCancelled = true
        }
    }

    /**
     * Registers the given [subCommand] as child of this [MineCommand].
     */
    fun registerSubCommand(subCommand: MineCommand) {
        subCommands.add(subCommand)
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
    internal fun executeCommand(sender: CommandSender, args: List<String>) {
        try {
            val subC = args.firstOrNull()?.lowercase()
            var sub: MineCommand? = null

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
                    if (sender is Player) {
                        sender.soundNo()
                    }
                    sender.sendMessage(sub.permissionMessage)
                    return
                }

                sub.command(sender, finallyArgs)
            } else {
                val commandPerm = permission
                if (commandPerm != null && !sender.hasPermission(commandPerm)) {
                    if (sender is Player) {
                        sender.soundNo()
                    }
                    sender.sendMessage(permissionMessage)
                    return
                }
                command(sender, args)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    // label = subcommand
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        executeCommand(sender, args?.toList() ?: emptyList()) // The command code itself is here
        return true
    }

    override fun onTabComplete(
        sender: CommandSender?,
        command: Command?,
        label: String?,
        args: Array<out String>?
    ): MutableList<String> {
        UtilsMain.instance.log("AutoComplete.")
        val data = mutableListOf<String>()

        if (args == null || args.isEmpty()) {
            data.addAll(subCommands.map { it.name })

            data.addAll(
                Mine.getPlayers().map { it.name }
            )
        } else {
            data.addAll(subCommands.map { it.name }.filter { it.lowercase().startsWith(args.last().lowercase()) })

            data.addAll(
                Mine.getPlayers().map { it.name }
                    .filter { it.lowercase().startsWith(args[0]) }
            )
        }

        return data
    }

    internal class SimpleCommand(val mineCommand: MineCommand) : Command(
        mineCommand.name
    ), PluginIdentifiableCommand {
        // Sets the plugin to our plugin so it shows up in /help
        override fun getPlugin(): JavaPlugin {
            return mineCommand.plugin
        }

        init {
            name = mineCommand.name
            aliases = mineCommand.aliases
            usage = mineCommand.usage
        }

        override fun execute(sender: CommandSender, label: String, args: Array<out String>?): Boolean {
            mineCommand.executeCommand(sender, args?.toList() ?: emptyList()) // The command code itself is here
            return true
        }

        override fun tabComplete(
            sender: CommandSender?,
            alias: String?,
            args: Array<out String>?
        ): MutableList<String> {
            return mineCommand.onTabComplete(sender, this, alias, args)
        }
    }
}