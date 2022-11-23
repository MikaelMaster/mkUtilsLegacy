package com.mikael.mkutilslegacy.spigot.api.lib.menu

import com.mikael.mkutilslegacy.api.isMultOf
import com.mikael.mkutilslegacy.api.mkplugin.MKPlugin
import com.mikael.mkutilslegacy.spigot.UtilsMain
import com.mikael.mkutilslegacy.spigot.api.*
import com.mikael.mkutilslegacy.spigot.api.lib.MineItem
import com.mikael.mkutilslegacy.spigot.api.lib.MineListener
import net.eduard.api.lib.modules.BukkitTimeHandler
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.scheduler.BukkitRunnable

/**
 * [MineMenu] util class
 *
 * This class extends a [MineListener].
 *
 * You can learn how to use it on [com.mikael.mkutilslegacy.spigot.api.lib.menu.example].
 * There, you'll find examples about how to create all different types of menu available.
 *
 * To create a new MineMenu, extends it in a Class. As the example below:
 * - class TestMenu : MineMenu(title: [String], lineAmount: [Int]) { *class code* }
 *
 * @author Mikael
 * @see MineListener
 * @see MenuButton
 * @see MenuPage
 * @see MenuSystem
 */
open class MineMenu(var title: String, var lineAmount: Int) : MineListener() {

    // Menu Properties - Start

    var isAutoUpdate = true // Auto update this menu
    var canReceiveItems = false // If the menu can 'receive items' from the player inventory in some icons

    // Auto Align options - Start
    var isAutoAlignItems = false
    var autoAlignIgnoreColumns = true
    var autoAlignSkipLines: List<Int> = emptyList()
    private val autoAlignPerPage: Int
        get() {
            var amount = lineAmount * 9
            if (!isAutoAlignItems) return amount
            for (n in 1..autoAlignSkipLines.size) amount -= 9
            if (autoAlignIgnoreColumns) amount -= lineAmount.minus(autoAlignSkipLines.size).times(2)
            return amount
        }
    // Auto Align options - End

    // Back and Skip Page buttons options - Start
    var backPageButtonPosX = 0
    var backPageButtonPosY = 1
    var backPageButtonItem = MineItem(Material.ARROW).name("§aPage %page%")
    var nextPageButtonPosX = 8
    var nextPageButtonPosY = 1
    var nextPageButtonItem = MineItem(Material.ARROW).name("§aPage %page%")
    // Back and Skip Page buttons options - End

    // Menu Properties - End

    val buttonsToRegister = mutableSetOf<MenuButton>()
    private val pages = mutableMapOf<Player, MutableList<MenuPage>>()
    private val inventories = mutableMapOf<Player, MutableMap<Int, Inventory>>()

    /**
     * @return A [List] of all [MenuButton]s inside this menu for each holder (player).
     */
    val buttons: Map<Player, List<MenuButton>>
        get() {
            val allButtons = mutableMapOf<Player, MutableList<MenuButton>>()
            for ((player, pages) in pages) {
                allButtons[player] = mutableListOf()
                for (page in pages) {
                    for (button in page.buttons) {
                        allButtons[player]!!.add(button)
                    }
                }
            }
            return allButtons
        }

    /**
     * Registers this [MineMenu]. Must be used on your plugin onEnable.
     *
     * @param plugin your plugin instance that is a [MKPlugin].
     * @return True if this [MineMenu] is successfully registered. Otherwise, false.
     * @see MineListener.registerListener
     * @see MenuSystem.registeredMenus
     */
    open fun registerMenu(plugin: MKPlugin): Boolean {
        if (lineAmount < 1 || lineAmount > 6) error("Menu lineAmount must be between 1 and 6")
        this.registerListener(plugin)
        MenuSystem.registeredMenus.add(this)
        return true
    }

    /**
     * Unregisters this [MineMenu]. Must be used on your plugin onDisable.
     *
     * You only should use this if needed.
     * For example: If you want that your plugin have support to /reload.
     *
     * Please note that all [Player]s seeing this [MineMenu] will have it closed.
     *
     * @return True if this [MineMenu] is successfully unregistered. Otherwise, false.
     * @see MineListener.unregisterListener
     */
    open fun unregisterMenu(): Boolean {
        MenuSystem.openedMenu.keys.removeIf {// Remove menu, pages and close it to players
            val menu = it.openedMineMenu!!
            if (menu == this) it.closeInventory()
            MenuSystem.openedPage.keys.removeIf { pagePlayer ->
                pages.containsKey(pagePlayer)
            }
            menu == this
        }
        this.unregisterListener()
        MenuSystem.registeredMenus.remove(this)
        return true
    }

    /**
     * @param player the [Player] to get opened [MineMenu] [MenuPage] ID.
     * @return The opened page ID, null if the player don't have a [MineMenu] opened.
     */
    fun getPageOpened(player: Player): Int? {
        return player.openedMineMenuPage?.pageId
    }

    /**
     * Remove all buttons from the menu. You should use it only inside [update] fun before register buttons.
     *
     * @param player the player (holder) of the menu to remove the buttons.
     */
    fun removeAllButtons(player: Player) {
        pages[player]?.let { pages -> pages.forEach { it.buttons.clear() } }
    }

    /**
     * Here all buttons and menu code will be done.
     *
     * @param player the player that will see this menu. All menus are per-player.
     */
    open fun update(player: Player) {
        // Do something
    }

    /**
     * Internal; Private.
     */
    private fun invokePageNextAndBackButtons(page: MenuPage) {
        val inv = page.inventory ?: error("Page's inventory can't be null")
        if (page.hasBackPage) {
            val backPageInv = page.backPage?.inventory ?: error("Menu previous page/inventory is null")
            val backButton = MenuButton("back-page").apply {
                autoEffectiveSlot = 0
                fixed = true
                icon = backPageButtonItem.clone().name(
                    backPageButtonItem.getName().replace("%page%", "${page.backPage!!.pageId}", true)
                )
                click = click@{
                    val player = it.player
                    player.soundClick(2f, 2f)
                    player.openInventory(backPageInv)
                }
            }
            inv.setItem(
                backButton.effectiveSlot,
                backButton.icon
            )
            page.backPageButton = backButton
            page.buttons.add(backButton)
        }
        if (page.hasNextPage) {
            val nextPageInv = page.nextPage?.inventory ?: error("Menu next page/inventory is null")
            val nextButton = MenuButton("next-page").apply {
                autoEffectiveSlot = 8
                fixed = true
                icon = nextPageButtonItem.clone().name(
                    nextPageButtonItem.getName().replace("%page%", "${page.nextPage!!.pageId}", true)
                )
                click = click@{
                    val player = it.player
                    player.soundClick(2f, 2f)
                    player.openInventory(nextPageInv)
                }
            }
            inv.setItem(
                nextButton.effectiveSlot,
                nextButton.icon
            )
            page.nextPageButton = nextButton
            page.buttons.add(nextButton)
        }
    }

    /**
     * Opens the menu for a player using the [MenuPage] ID 1.
     *
     * @param player the player to open the menu.
     * @return the [Inventory] builder of the [MenuPage].
     * @throws IllegalStateException if the menu [autoAlignSkipLines] contains any Int different from 1, 2, 3, 4, 5 and 6.
     * @throws IllegalStateException if the menu [autoAlignSkipLines] is higher than the menu [lineAmount].
     */
    fun open(player: Player): Inventory {
        return open(player, 1)
    }

    /**
     * Opens the menu for a player using the given [pageToOpen].
     *
     * @param player the player to open the menu.
     * @param pageToOpen the [MenuPage] ID to open to the given [player].
     * @return the [Inventory] builder of the [MenuPage].
     * @throws IllegalStateException if the [lineAmount] is not between 1 and 6.
     * @throws IllegalStateException if the menu [isAutoAlignItems] is false, and the [pageToOpen] is not 1.
     * @throws IllegalStateException if the menu [autoAlignSkipLines] contains any Int different from 1, 2, 3, 4, 5 and 6.
     * @throws IllegalStateException if the menu [autoAlignSkipLines] is higher than the menu [lineAmount].
     */
    fun open(player: Player, pageToOpen: Int): Inventory {
        update(player) // Rebuilds menu
        if (lineAmount < 1 || lineAmount > 6) error("Menu lineAmount must be between 1 and 6")
        if (!isAutoAlignItems && pageToOpen != 1) error("Cannot open a non-autoAlignItems menu with a page different than 1")
        // if (pageToOpen > 1 && inventories[pageToOpen] == null) error("the required page $pageToOpen is not registered; pages size: ${pages.size}") // legacy
        autoAlignSkipLines.forEach {
            if (it != 1 && it != 2 && it != 3 && it != 4 && it != 5 && it != 6) error("Menu autoAlignSkipLines can't contains any Int different from 1, 2, 3, 4, 5 and 6")
            if (it > lineAmount) error("This menu just have $lineAmount lines, can't apply rule to skip line $it")
        }

        val playerPages = pages.getOrPut(player) { mutableListOf() }
        val playerInventories = inventories.getOrPut(player) { mutableMapOf() }

        for (page in playerPages) {
            page.buttons.clear()
            page.inventory = null
        }
        playerPages.clear()
        playerInventories.clear()

        if (isAutoAlignItems) {
            var lastInv: Inventory =
                Bukkit.createInventory(
                    null,
                    9 * lineAmount,
                    title.replace("%page%", playerPages.size.plus(1).toString(), true)
                )
            var lastPage = MenuPage()
            lastPage.pageId = playerPages.size.plus(1)
            lastPage.inventory = lastInv
            playerPages.add(lastPage)
            playerInventories[playerPages.size] = lastInv
            var lastSlot = 0
            var buttonId = 1

            for (button in buttonsToRegister.filter { !it.fixed }) {
                if (lastPage.buttons.filter { !it.fixed }.size >= autoAlignPerPage) {
                    lastInv =
                        Bukkit.createInventory(
                            null,
                            9 * lineAmount,
                            title.replace("%page%", playerPages.size.plus(1).toString(), true)
                        )
                    val lp = lastPage
                    lastPage.hasNextPage = true
                    lastPage = MenuPage()
                    lastPage.pageId = playerPages.size.plus(1)
                    lastPage.inventory = lastInv
                    lastPage.hasBackPage = true
                    lastPage.backPage = lp
                    lp.nextPage = lastPage
                    playerPages.add(lastPage)
                    playerInventories[playerPages.size] = lastInv
                    lastSlot = 0 // reset count
                    buttonId = 1 // reset count
                }
                if (button.icon != null) {
                    var buttonSlot = button.effectiveSlot
                    if (autoAlignSkipLines.isNotEmpty()) {
                        if (lastSlot == 0) {
                            var lastSkip = 0
                            skip@ for (skip in autoAlignSkipLines) {
                                if (lastSkip.plus(1) != skip) break@skip
                                lastSlot += if (autoAlignIgnoreColumns && lastSlot == 1) 10 else 9
                                lastSkip++
                            }
                        }
                    } else if (lastSlot == 0 && autoAlignIgnoreColumns) {
                        lastSlot = 1
                    }
                    if (lastSlot.plus(1) < 9 * lineAmount) {
                        lastSlot++
                    }
                    button.menuId = buttonId
                    val idToVerify = buttonId.minus(1)
                    if (autoAlignIgnoreColumns && idToVerify != 0 &&
                        idToVerify != 1 &&
                        idToVerify.isMultOf(7) && lastSlot.plus(1) < 9 * lineAmount
                    ) {
                        lastSlot += 2
                    }
                    buttonSlot = lastSlot
                    button.autoEffectiveSlot = buttonSlot
                    buttonId++
                    lastInv.setItem(buttonSlot, button.icon)
                    lastPage.buttons.add(button)
                }
            }
            playerPages.forEach { menuPage ->
                invokePageNextAndBackButtons(menuPage)
                for (fixedButton in buttonsToRegister.filter { it.fixed }) {
                    menuPage.inventory!!.setItem(fixedButton.effectiveSlot, fixedButton.icon)
                    menuPage.buttons.add(fixedButton)
                }
                menuPage.buttons.forEach { pageButton ->
                    pageButton.inventory = menuPage.inventory!!
                }
            }
            buttonsToRegister.clear()
            val choosenInv =
                playerInventories[pageToOpen] ?: error("Cannot open page $pageToOpen; pages size: ${pages.size}")
            player.openInventory(choosenInv)
            player.openedMineMenu = this
            player.openedMineMenuPage = playerPages.first { it.pageId == pageToOpen }
            return choosenInv
        } else {
            val singlePage = MenuPage()
            val pageInventory =
                playerInventories.getOrDefault(
                    pageToOpen,
                    Bukkit.createInventory(
                        null,
                        9 * lineAmount,
                        title.replace("%page%", playerPages.size.plus(1).toString(), true)
                    )
                )
            pageInventory.clear()
            singlePage.inventory = pageInventory
            playerInventories[pageToOpen] = pageInventory
            playerPages.add(singlePage)

            for (button in buttonsToRegister) {
                if (button.icon != null) {
                    pageInventory.setItem(button.effectiveSlot, button.icon)
                    singlePage.buttons.add(button)
                }
            }
            buttonsToRegister.clear()
            player.openInventory(pageInventory)
            player.openedMineMenu = this
            player.openedMineMenuPage = singlePage
            return pageInventory
        }
    }

    /**
     * Creates a new button inside the menu.
     *
     * @param buttonName the button name. Can be null.
     * @param fixed if this button will be auto align or not.
     * @param x the icon X position. Let it as null if [fixed] is false.
     * @param y the icon Y position. Let it as null if [fixed] is false.
     * @param setup the [MenuButton] builder.
     * @return the built [MenuButton].
     */
    fun button(
        buttonName: String? = null,
        fixed: Boolean = !isAutoAlignItems,
        x: Int = 1,
        y: Int = 1,
        setup: (MenuButton.() -> Unit)
    ): MenuButton {
        val newButton = if (buttonName != null) MenuButton(buttonName) else MenuButton()
        newButton.setup()
        newButton.fixed = fixed
        if (fixed) {
            newButton.positionX = x
            newButton.positionY = y
        }
        buttonsToRegister.add(newButton)
        return newButton
    }

    /**
     * Creates a new animated [MenuButton] inside the menu.
     *
     * @param buttonName the button name. Can be null.
     * @param fixed if this button will be auto align or not.
     * @param x the icon X position. Let it as null if [fixed] is false.
     * @param y the icon Y position. Let it as null if [fixed] is false.
     * @param changeFrameDelay the time to change between icons (frames). Defined in ticks (20 = 1s).
     * @param setup the [MenuButton] builder.
     * @return the built [MenuButton].
     */
    fun animatedButton(
        buttonName: String? = null,
        fixed: Boolean = !isAutoAlignItems,
        x: Int = 1,
        y: Int = 1,
        changeFrameDelay: Long = 20,
        setup: (MenuButton.() -> Unit)
    ): MenuButton {
        val newButton = if (buttonName != null) MenuButton(buttonName) else MenuButton()
        newButton.setup()
        newButton.fixed = fixed
        if (fixed) {
            newButton.positionX = x
            newButton.positionY = y
        }
        object : BukkitRunnable() {
            override fun run() {
                newButton.inventory?.let { buttonInv ->
                    if (newButton.lastFrameId + 1 > newButton.frames.size) {
                        newButton.lastFrameId = 0
                    }
                    newButton.icon = newButton.frames[newButton.lastFrameId]
                    newButton.lastFrameId++

                    val currentAutoSlot = newButton.autoEffectiveSlot
                    if (currentAutoSlot != null) {
                        buttonInv.setItem(currentAutoSlot, newButton.icon)
                    } else {
                        buttonInv.setItem(newButton.effectiveSlot, newButton.icon)
                    }

                    if (buttonInv.viewers.isEmpty()) cancel()
                }
            }
        }.runTaskTimer(UtilsMain.instance, changeFrameDelay, changeFrameDelay)
        buttonsToRegister.add(newButton)
        return newButton
    }

    /**
     * Creates a new [MenuButton] inside this menu with will be built using async.
     *
     * This will process the code inside [setup] using async, then
     * will update the button using its data once it's complete.
     *
     * @param buttonName the button name. Can be null.
     * @param fixed if this button will be auto align or not.
     * @param x the icon X position. Let it as null if [fixed] is false.
     * @param y the icon Y position. Let it as null if [fixed] is false.
     * @param setup the [MenuButton] builder.
     * @return the built [MenuButton]. This button will be changed once [setup] is complete run using async.
     * @see [BukkitTimeHandler.asyncDelay]
     * @see [BukkitTimeHandler.syncTask]
     */
    fun asyncButton(
        buttonName: String? = null,
        fixed: Boolean = !isAutoAlignItems,
        x: Int = 1,
        y: Int = 1,
        setup: (MenuButton.() -> Unit)
    ): MenuButton {
        val newButton = if (buttonName != null) MenuButton(buttonName) else MenuButton()
        newButton.icon = MineItem(Material.STAINED_GLASS_PANE).data(7).name("§8Loading...")
        newButton.fixed = fixed
        if (fixed) {
            newButton.positionX = x
            newButton.positionY = y
        }
        buttonsToRegister.add(newButton)
        UtilsMain.instance.asyncDelay(1) {
            try {
                newButton.setup()
                UtilsMain.instance.syncTask {
                    try {
                        newButton.inventory?.let { buttonInv ->
                            val currentAutoSlot = newButton.autoEffectiveSlot
                            if (currentAutoSlot != null) {
                                buttonInv.setItem(currentAutoSlot, newButton.icon)
                            } else {
                                buttonInv.setItem(newButton.effectiveSlot, newButton.icon)
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return newButton
    }

    /**
     * Listeners section.
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInvClose(e: InventoryCloseEvent) { // PlayerQuitEvent is not used anymore, onInvClose already clear all lists on quit.
        if (e.player !is Player) return
        val player = e.player as Player
        player.runBlock {
            val playerPages = pages[player] ?: return@runBlock
            if (!playerPages.any { e.inventory == it.inventory!! }) return@runBlock
            if (playerPages.any { it.nextPage != null && it.nextPage!!.inventory == player.openInventory }) {
                pages.remove(player)
                inventories.remove(player)
            }
            player.openedMineMenu = null // MenuSystem.openedMenu.remove(player)
            player.openedMineMenuPage = null // MenuSystem.openedPage.remove(player)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInvClick(e: InventoryClickEvent) {
        if (e.clickedInventory == null) return
        val player = e.player
        player.runBlock {
            if (player.openedMineMenu != this) return@runBlock
            val playerPages = pages[player] ?: return@runBlock
            val clickedPage = playerPages.firstOrNull { e.clickedInventory == it.inventory!! }
            clickedPage?.buttons?.firstOrNull { e.slot == it.effectiveSlot }?.click?.invoke(e)
            if (clickedPage == null && canReceiveItems) return@runBlock
            e.isCancelled = true
        }
    }
}