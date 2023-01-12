package com.mikael.mkutilslegacy.spigot.api

import com.mikael.mkutilslegacy.api.formatPersonal
import com.mikael.mkutilslegacy.api.formatValue
import com.mikael.mkutilslegacy.api.mkplugin.MKPlugin
import com.mikael.mkutilslegacy.spigot.UtilsMain
import com.mikael.mkutilslegacy.spigot.api.lib.MineItem
import com.mikael.mkutilslegacy.spigot.api.lib.book.MineBook
import com.mikael.mkutilslegacy.spigot.api.lib.hooks.Vault
import com.mikael.mkutilslegacy.spigot.api.lib.menu.MenuPage
import com.mikael.mkutilslegacy.spigot.api.lib.menu.MenuSystem
import com.mikael.mkutilslegacy.spigot.api.lib.menu.MineMenu
import com.mikael.mkutilslegacy.spigot.listener.GeneralListener
import net.eduard.api.lib.kotlin.mineSendActionBar
import net.eduard.api.lib.kotlin.mineSendTitle
import net.eduard.api.lib.kotlin.sendTitle
import net.eduard.api.lib.modules.MineReflect
import net.minecraft.server.v1_8_R3.PacketPlayOutExplosion
import net.minecraft.server.v1_8_R3.Vec3D
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity
import org.bukkit.entity.*
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.Vector
import java.util.*

/**
 * Shortcut to get the [UtilsMain.instance].
 *
 * @return the [UtilsMain.instance].
 */
val utilsMain get() = UtilsMain.instance

// MineBook extra functions - Start
/**
 * Opens the [book] to the given [Player].
 *
 * Note: The function [MineBook.open] uses NMS. (NMS 1.8_R3)
 *
 * @see MineBook.open
 */
fun Player.openMineBook(book: MineBook) {
    book.open(this)
}
// MineBook extra functions - End

// MineMenu extra functions - Start
/**
 * Opens the [menu] to the given [Player].
 *
 * @return the opened [Inventory] owned by the [MineMenu] ([menu]).
 * @see MineMenu.open
 */
fun Player.openMineMenu(menu: MineMenu): Inventory {
    return MenuSystem.openMenu(this, menu)
}

/**
 * It'll check if the value returned from [Player.openedMineMenu] is not null.
 * If it's not, and the returned menu is the given [menu] the [player] have an opened [MineMenu].
 *
 * Note: This is a shortcut of [MenuSystem.isMenuOpen].
 *
 * @return True if the given [Player] is with the given [menu] opened. Otherwise, false.
 * @see MineMenu
 * @see Player.openedMineMenu
 * @see MenuSystem
 */
fun Player.isMineMenuOpen(menu: MineMenu): Boolean {
    return MenuSystem.isMenuOpen(menu, this)
}

/**
 * Sets/returns player's opened [MineMenu].
 *
 * Note: 'Set' option is *internal only*.
 *
 * @return Player's opened [MineMenu]?.
 * @see MineMenu
 */
var Player.openedMineMenu: MineMenu?
    get() = MenuSystem.openedMenu[this]
    internal set(value) {
        if (value == null) {
            MenuSystem.openedMenu.remove(this)
        } else {
            MenuSystem.openedMenu[this] = value
        }
    }

/**
 * Sets/returns player's opened [MenuPage].
 *
 * Note: 'Set' option is *internal only*.
 *
 * @return Player's opened [MenuPage]?.
 * @see MenuPage
 */
var Player.openedMineMenuPage: MenuPage?
    get() = MenuSystem.openedPage[this]
    internal set(value) {
        if (value == null) {
            MenuSystem.openedPage.remove(this)
        } else {
            MenuSystem.openedPage[this] = value
        }
    }
// MineMenu extra functions - End

/**
 * Cashes the given [Player] client. USE WITH MODERATION!
 *
 * This will send a packet to the player ([MineReflect.sendPacket]) using parameters
 * with no-sense values like [Double.MAX_VALUE] in the size of the explosion ([PacketPlayOutExplosion]).
 * And this 'crazy' values will crash the player client.
 *
 * @param plugin the [MKPlugin] that wants to crash the given [Player].
 * @return True of the 'crash-packet' was successfully sent. Otherwise, false.
 */
fun Player.crashClient(plugin: MKPlugin): Boolean {
    utilsMain.log("§c[Player Crasher] §eCrashing player ${this.name.formatPersonal()} client. (Request by plugin: ${plugin.systemName})")
    try {
        MineReflect.sendPacket(
            this, PacketPlayOutExplosion(
                Double.MAX_VALUE,
                Double.MAX_VALUE,
                Double.MAX_VALUE,
                Float.MAX_VALUE,
                emptyList(),
                Vec3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)
            )
        )
    } catch (ex: Exception) {
        ex.printStackTrace()
        utilsMain.log("§c[Player Crasher] §cAn internal error occurred while crashing a player. (Request by plugin: ${plugin.systemName})")
    }
    return true
}

/**
 * @return The player that clicked the menu. ([InventoryClickEvent.getWhoClicked] as [Player])
 * @see InventoryClickEvent.getWhoClicked
 */
val InventoryClickEvent.player get() = this.whoClicked as Player

/**
 * Adds lines to the given [ItemStack].
 *
 * Note: If you're using mkUtils, prefer use [MineItem] instead of [ItemStack].
 *
 * So, then use [MineItem.addLore]. You can transform an [ItemStack] to a Mine Item using [ItemStack.toMineItem].
 *
 * @param lines the lines of string to add.
 * @return the given [ItemStack] with the new lines added.
 * @see ItemMeta.setLore
 */
fun <T : ItemStack> T.addLore(vararg lines: String): T {
    val meta = this.itemMeta!!
    if (meta.lore == null) meta.lore = emptyList()
    val newLore = mutableListOf<String>()
    for (line in meta.lore!!) {
        newLore.add(line)
    }
    for (newLine in lines) {
        newLore.add(newLine)
    }
    meta.lore = newLore
    this.itemMeta = meta
    return this
}

var AGEABLE_FORMAT_AGE_TEXT_ADULT = "Adult"
var AGEABLE_FORMAT_AGE_TEXT_BABY = "Baby"

/**
 * @return if [Ageable.isAdult] '[AGEABLE_FORMAT_AGE_TEXT_ADULT]' else '[AGEABLE_FORMAT_AGE_TEXT_BABY]'.
 */
fun Ageable.formatAgeText(): String {
    return if (this.isAdult) AGEABLE_FORMAT_AGE_TEXT_ADULT else AGEABLE_FORMAT_AGE_TEXT_BABY
}

/**
 * @return A new [MineItem] cloning the given [ItemStack].
 * @see MineItem
 */
fun ItemStack.toMineItem(): MineItem {
    return MineItem(this)
}

/**
 * This will disable the AI of the given [Entity].
 * An entity with the AI disabled will do nothing, don't even move.
 *
 * WARNING: There is *NO WAY to activate the entity's AI again*. (This will be added in the future)
 *
 * @return the given [Entity], now with the AI disabled.
 * @see MineReflect.disableAI
 */
fun Entity.disableAI(): Entity {
    MineReflect.disableAI(this)
    return this
}

/**
 * Note: Deprecated because some entities don't work well with this function, and may throw errors.
 * You should use your own method instead.
 *
 * @return the given [Entity], now invincible.
 * @see Entity.isInvincible
 */
fun Entity.setInvincible(isInvincible: Boolean): Entity {
    if (isInvincible) {
        GeneralListener.instance.invincibleEntities.add(this)
    } else {
        GeneralListener.instance.invincibleEntities.remove(this)
    }
    return this
}

/**
 * @return True if [GeneralListener.invincibleEntities] contains the given [Entity]. Otherwise, false.
 * @see Entity.setInvincible
 */
val Entity.isInvincible: Boolean get() = GeneralListener.instance.invincibleEntities.contains(this)

/**
 * Important: This function is not 100% tested and *MAY NOT work well with some entities*.
 *
 * @return True if this entity is a peaceful entity. Otherwise, false.
 */
val Entity.isPeaceful: Boolean
    get() {
        if (this is Creature) {
            return this !is Monster
        }
        return true
    }

/**
 * @return True if the player's inventory have free slots. Otherwise, false.
 */
val Player.hasFreeSlots: Boolean get() = this.freeSlots > 0

/**
 * @return The amount of free slots in this player's inventory.
 */
val Player.freeSlots: Int get() = 36 - this.inventory.contents.size

/**
 * @return True if the player has the needed amount of the needed ItemStack on his inventory.
 */
fun Player.hasAmountOfItemOnInv(needed: ItemStack, neededAmount: Int): Boolean {
    return this.inventory.hasAmountOfItem(needed, neededAmount)
}

/**
 * @return True if the given [Inventory] has the needed amount of the needed [ItemStack].
 */
fun Inventory.hasAmountOfItem(needed: ItemStack, neededAmount: Int): Boolean {
    if (this.contents.isEmpty()) return false
    var amount = 0
    for (item in this) {
        if (item == null) continue
        if (needed == item) {
            amount++
            if (amount >= neededAmount) return true
        }
    }
    return false
}

/**
 * Turns the given [ItemStack] not breakable.
 *
 * @param isUnbreakable if the item will be or not unbreakable. By default, True.
 * @return The new not breakable [ItemStack].
 * @see ItemMeta.Spigot.isUnbreakable
 */
fun <T : ItemStack> T.notBreakable(isUnbreakable: Boolean = true): T {
    val meta = itemMeta!!
    meta.spigot().isUnbreakable = isUnbreakable
    itemMeta = meta
    return this
}

/**
 * Runs a 'blood' effect on a [Player]'s body.
 *
 * @param allBody if is to spawn the 'blood' particle on player Head AND Foot. If false the 'blood' particle will be spawned JUST on [Player.getEyeLocation].
 * @return True if the player is not dead, and the effect was played. Otherwise, false.
 * @see World.playEffect
 * @see Effect.STEP_SOUND
 * @see Material.REDSTONE_BLOCK (as breaked block, using STEP_SOUND effect)
 */
fun Player.bloodEffect(allBody: Boolean = false): Boolean {
    if (this.isDead) return false
    this.world.playEffect(this.eyeLocation, Effect.STEP_SOUND, Material.REDSTONE_BLOCK)
    if (allBody) {
        this.world.playEffect(this.location, Effect.STEP_SOUND, Material.REDSTONE_BLOCK)
    }
    return true
}

/**
 * Plays the sound [Sound.VILLAGER_NO] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.soundNo(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.VILLAGER_NO, volume, speed)
}

/**
 * Plays the sound [Sound.VILLAGER_YES] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.soundYes(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.VILLAGER_YES, volume, speed)
}

/**
 * Plays the sound [Sound.CLICK] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.soundClick(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.CLICK, volume, speed)
}

/**
 * Plays the sound [Sound.ITEM_PICKUP] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.soundPickup(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.ITEM_PICKUP, volume, speed)
}

/**
 * Plays the sound [Sound.NOTE_PLING] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.soundPling(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.NOTE_PLING, volume, speed)
}

/**
 * Plays the sound [Sound.ORB_PICKUP] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.notify(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.ORB_PICKUP, volume, speed)
}

/**
 * Plays the sound [Sound.ENDERMAN_TELEPORT] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.soundTP(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.ENDERMAN_TELEPORT, volume, speed)
}

/**
 * Gives an item to a [Player] if there is an available slot on his inventory.
 * If there is no empty slot, the [ItemStack] will be dropped on the world, using the [Player]'s eye location.
 *
 * @param item the [ItemStack] to be added on [Player]'s [Inventory]. (It will be dropped if the inventory is full)
 * @return A dropped [Item] if the [Player]'s [Inventory] is full. Otherwise, null.
 * @see Item
 */
fun Player.giveItem(item: ItemStack): Item? {
    val itemClone = item.clone()
    if (itemClone.amount !in 1..64) error("Cannot give an ItemStack with amount less than 1 or higher than 64 (item amount: ${itemClone.amount})")
    invSlot@ for (invItem in this.inventory.contents) {
        if (invItem == null) continue@invSlot
        val maxStackSize = invItem.type.maxStackSize
        if (itemClone.isSimilar(invItem) && invItem.amount < maxStackSize) {
            val sumAmount = invItem.amount + itemClone.amount
            if (sumAmount <= maxStackSize) {
                invItem.amount += itemClone.amount
            } else {
                invItem.amount = maxStackSize
                itemClone.amount = sumAmount - maxStackSize
                return this.giveItem(itemClone)
            }
            return null
        } else {
            continue@invSlot
        }
    }
    val slot = this.inventory.contents.withIndex().firstOrNull { it.value == null && it.index < 36 }
        ?: return this.world.dropItemNaturally(this.eyeLocation, itemClone)
    this.inventory.setItem(slot.index, itemClone)
    this.updateInventory()
    return null
}

/**
 * Gives an Armor Set to a player if all his equipment slots is available.
 * If there is no equipment slots available, the [ItemStack]s will be dropped on the world, using the given [Player]'s eye location.
 *
 * Tip: To know if all the armor has been successfully set on the player, just verify if the returned [List] with [Item]s is empty.
 *
 * @return A list of dropped [Item]s with the Armors that cannot be given to the [Player].
 * @see Item
 */
fun Player.giveArmorSet(
    helmet: ItemStack?,
    chestplate: ItemStack?,
    leggings: ItemStack?,
    boots: ItemStack?
): List<Item> {
    val droppedArmor = mutableListOf<Item>()
    if (helmet != null) {
        if (this.inventory.helmet != null) {
            droppedArmor.add(this.world.dropItemNaturally(this.eyeLocation, helmet))
        } else {
            this.inventory.helmet = helmet
        }
    }
    if (chestplate != null) {
        if (this.inventory.chestplate != null) {
            droppedArmor.add(this.world.dropItemNaturally(this.eyeLocation, chestplate))
        } else {
            this.inventory.chestplate = helmet
        }
    }
    if (leggings != null) {
        if (this.inventory.leggings != null) {
            droppedArmor.add(this.world.dropItemNaturally(this.eyeLocation, leggings))
        } else {
            this.inventory.leggings = helmet
        }
    }
    if (boots != null) {
        if (this.inventory.boots != null) {
            droppedArmor.add(this.world.dropItemNaturally(this.eyeLocation, boots))
        } else {
            this.inventory.boots = helmet
        }
    }
    return droppedArmor
}

var PLAYER_RUN_BLOCK_ERROR_MSG = "§cAn internal error occurred while executing something to you."

/**
 * Runs a loading animation to the player using the main thread (sync), while execute the given [thing] using async.
 *
 * Uses [PLAYER_RUN_BLOCK_ERROR_MSG] to send the player a message if an error occur.
 *
 * @param thing the block code to run using async, try catch and the load animation.
 */
inline fun Player.asyncLoading(crossinline thing: (() -> Unit)) {
    val runStart = System.currentTimeMillis()
    var step = 0
    val runnable = utilsMain.syncTimer(0, 2) {
        when (step) {
            0 -> {
                this.actionBar("§a∎§7∎∎∎∎")
            }

            1 -> {
                this.actionBar("§7∎§a∎§7∎∎∎")
            }

            2 -> {
                this.actionBar("§7∎∎§a∎§7∎∎")
            }

            3 -> {
                this.actionBar("§7∎∎∎§a∎§7∎")
            }

            4 -> {
                this.actionBar("§7∎∎∎∎§a∎")
            }
        }
        if (step == 4) step = 0 else step++
    }
    utilsMain.asyncTask {
        try {
            thing.invoke()
        } catch (ex: Exception) {
            ex.printStackTrace()
            this.soundNo()
            this.sendMessage(PLAYER_RUN_BLOCK_ERROR_MSG)
        } finally {
            utilsMain.syncTask {
                runnable.cancel()
                this.actionBar("§a∎∎∎∎∎ §8${(System.currentTimeMillis() - runStart).toInt().formatValue()}ms")
            }
        }
    }
}

/**
 * Runs a loading animation to the player using an async thread, while execute the given [thing] using sync (main thread).
 *
 * Please note that the given [thing] will be run 5 ticks (approximately 250ms) after the function ram.
 * This is to avoid actionbar-animation internal errors.
 *
 * Uses [PLAYER_RUN_BLOCK_ERROR_MSG] to send the player a message if an error occur.
 *
 * @param thing the block code to run using the main thread (sync), try catch and the load animation.
 */
inline fun Player.syncLoading(crossinline thing: (() -> Unit)) {
    val runStart = System.currentTimeMillis()
    var step = 0
    var animating = true
    utilsMain.asyncTask {
        while (animating) {
            when (step) {
                0 -> {
                    this.actionBar("§a∎§7∎∎∎∎")
                }

                1 -> {
                    this.actionBar("§7∎§a∎§7∎∎∎")
                }

                2 -> {
                    this.actionBar("§7∎∎§a∎§7∎∎")
                }

                3 -> {
                    this.actionBar("§7∎∎∎§a∎§7∎")
                }

                4 -> {
                    this.actionBar("§7∎∎∎∎§a∎")
                }
            }
            if (step == 4) step = 0 else step++
            Thread.sleep(100)
        }
    }
    utilsMain.syncDelay(5) {
        try {
            thing.invoke()
        } catch (ex: Exception) {
            ex.printStackTrace()
            this.soundNo()
            this.sendMessage(PLAYER_RUN_BLOCK_ERROR_MSG)
        } finally {
            animating = false
            this.actionBar("§a∎∎∎∎∎ §8${(System.currentTimeMillis() - runStart).toInt().formatValue()}ms")
        }
    }
}

/**
 * Use it anywhere to run the Unit using a try catch. If any error occur,
 * the given [Player] will receive a message saying that an error occurred.
 *
 * Uses [PLAYER_RUN_BLOCK_ERROR_MSG] to send the player a message if an error occur.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was run with no errors. Otherwise, false.
 */
inline fun Player.runBlock(crossinline thing: (() -> Unit)): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.soundNo()
        this.sendMessage(PLAYER_RUN_BLOCK_ERROR_MSG)
        false
    }
}

var RUN_COMMAND_ERROR_MSG = "§cAn internal error occurred while executing this command."

/**
 * Use in a command to run the Unit using a try catch. If any error occur,
 * the given [CommandSender] (can be the Console) will receive a message saying that an error occurred.
 *
 * If the given [CommandSender] is a [Player], [Player.runCommand] will be called internally.
 *
 * Uses [RUN_COMMAND_ERROR_MSG] to send the command sender a message if an error occur.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was run with no errors. Otherwise, false.
 */
inline fun CommandSender.runCommand(crossinline thing: (() -> Unit)): Boolean {
    if (this is Player) {
        return this.runCommand(thing)
    }
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.sendMessage(RUN_COMMAND_ERROR_MSG)
        false
    }
}

/**
 * Use in a command to run the Unit using a try catch. If any error occur,
 * the given [Player] will receive a message saying that an error occurred.
 *
 * Uses [RUN_COMMAND_ERROR_MSG] to send the player a message if an error occur.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was run with no errors. Otherwise, false.
 */
inline fun Player.runCommand(crossinline thing: (() -> Unit)): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.soundNo()
        this.sendMessage(RUN_COMMAND_ERROR_MSG)
        false
    }
}

/**
 * Clear all the player's inventory and armors.
 *
 * @param resetHoldSlot if is to set the player's hold item slot to 0.
 */
fun Player.clearAllInventory(resetHoldSlot: Boolean = true) {
    if (resetHoldSlot) {
        this.inventory.heldItemSlot = 0
    }
    this.inventory.clear()
    this.inventory.armorContents = arrayOf() // Clear armors
}

/**
 * Clear the player's action bar.
 */
fun Player.clearActionBar() {
    this.mineSendActionBar(" ")
}

/**
 * Send an action bar to the given player.
 *
 * @param msg the message to send on player's action bar.
 */
fun Player.actionBar(msg: String) {
    this.mineSendActionBar(msg)
}

/**
 * Clears the given [Player] client title field.
 *
 * @see Player.resetTitle
 * @suppress to ingore internal deprecated.
 */
@Suppress("DEPRECATION")
fun Player.clearTitle() {
    this.resetTitle()
}

/**
 * In-dev; Not done yet.
 */
@Deprecated("In-dev; Not done yet.")
internal fun Player.moveToMounted(toMovePlayer: Location, entityInvisible: Boolean = true) {
    val from = this.location
    val velocity = Vector(0, 0, 0)

    val horse = this.world.spawnEntity(from, EntityType.HORSE) as Horse
    horse.isTamed = true
    horse.variant = Horse.Variant.HORSE
    horse.setAdult()
    horse.inventory.saddle = MineItem(Material.SADDLE)
    horse.owner = this
    horse.passenger = this

    if (entityInvisible) {
        (horse as CraftEntity).handle.isInvisible = true
    }

    val to = toMovePlayer
    val distance = from.distance(to)
    val direction = to.toVector().subtract(from.toVector()).normalize()
    val step = direction.multiply(0.5)
    val ticks = (distance * 20 / 0.5).toInt()
    val steps = (ticks / 2)
    var ticksDelay = 1L

    for (i in 0..steps) {
        utilsMain.syncDelay(ticksDelay) {
            if (horse.passenger == null) {
                horse.passenger = this
            }
            velocity.add(step)
            horse.velocity = velocity
            ticksDelay++
        }
    }

    utilsMain.syncDelay(steps + 1L) {
        horse.remove()

    }
}

/**
 * This will 'launch' to the given [targetLoc] using the given parameters.
 *
 * @see Vector
 * @see Player.setVelocity
 */
fun Player.moveTo(targetLoc: Location, xzForce: Double = 4.0, yForce: Double = 1.0, soundEffect: Boolean = true) {
    val speed = targetLoc.toVector().subtract(this.location.toVector()).normalize().multiply(xzForce)
    speed.y = yForce
    if (soundEffect) {
        this.playSound(this.location, Sound.FIREWORK_LAUNCH, 2f, 2f)
    }
    this.velocity = speed
}

/**
 * Sends a [title] and [subtitle] to the given [Player].
 *
 * @see Player.sendTitle
 */
fun Player.title(title: String?, subtitle: String?, fadeIn: Int = 10, stay: Int = 20 * 2, fadeOut: Int = 10) {
    this.mineSendTitle(title ?: " ", subtitle ?: " ", fadeIn, stay, fadeOut)
}

/**
 * Sends to the given [Player] all given [messages].
 */
fun Player.sendMessage(vararg messages: String) {
    this.sendMessage(messages)
}

/**
 * @see [Vault.getPlayerBalance]
 */
fun Player.getVaultBalance(): Double {
    return Vault.getPlayerBalance(this)
}

/**
 * @see [Vault.setPlayerBalance]
 */
fun Player.setVaultBalance(amount: Double) {
    Vault.setPlayerBalance(this, amount)
}

/**
 * @see [Vault.addPlayerBalance]
 */
fun Player.addVaultBalance(amount: Double) {
    Vault.addPlayerBalance(this, amount)
}

/**
 * @see [Vault.removePlayerBalance]
 */
fun Player.removeVaultBalance(amount: Double) {
    Vault.removePlayerBalance(this, amount)
}

/**
 * @return a new location where X,Y,Z are the center of the [Location.getBlock].
 */
fun Location.toCenterLocation(): Location {
    val centerLoc = this.clone()
    centerLoc.x = blockX + 0.5
    centerLoc.y = blockY + 0.5
    centerLoc.z = blockZ + 0.5
    return centerLoc
}

/**
 * Get all blocks inside the given chunk.
 *
 * Use with moderation.
 * May lag the server with many usages at the same time.
 *
 * @return all the chunk [Block]s.
 * @see Chunk
 * @see Block
 */
val Chunk.blocks: List<Block>
    get() {
        val blocks = mutableListOf<Block>()
        for (x in 0..15) {
            for (y in 0..255) {
                for (z in 0..15) {
                    blocks.add(this.getBlock(x, y, z))
                }
            }
        }
        return blocks
    }

/**
 * @return the given [Entity]'s chunk. (Entity.[Location.getChunk])
 */
val Entity.chunk: Chunk get() = this.location.chunk