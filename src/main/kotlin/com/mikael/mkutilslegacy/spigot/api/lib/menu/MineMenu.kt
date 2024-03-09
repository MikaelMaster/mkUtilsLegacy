package com.mikael.mkutilslegacy.spigot.api.lib.menu

import com.mikael.mkutilslegacy.api.isMultOf
import com.mikael.mkutilslegacy.api.mkplugin.MKPlugin
import com.mikael.mkutilslegacy.spigot.api.*
import com.mikael.mkutilslegacy.spigot.api.event.menu.MineMenuOpenEvent
import com.mikael.mkutilslegacy.spigot.api.lib.MineItem
import com.mikael.mkutilslegacy.spigot.api.lib.MineListener
import com.mikael.mkutilslegacy.spigot.api.lib.menu.button.MenuAnimatedButton
import com.mikael.mkutilslegacy.spigot.api.lib.menu.button.MenuButton
import net.eduard.api.lib.kotlin.mineCallEvent
import net.eduard.api.lib.modules.BukkitTimeHandler
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Represents a mkUtils 'menu' created using Bukkit Inventories.
 *
 * This class extends a [MineListener].
 *
 * To create a new MineMenu, extends it in a Class. As the example below:
 * - class TestMenu : MineMenu(title: [String], lineAmount: [Int]) { *class code* }
 *
 * @author Mikael
 * @see MineListener
 * @see MenuButton
 * @see AnimatedMenuButton
 * @see MenuPage
 * @see MenuSystem
 */
@Suppress("WARNINGS")
open class MineMenu(var title: String, var lineAmount: Int) : MineListener() {

    // Menu Properties - Start
    var isAutoUpdate = true // Auto update this menu
    var canReceiveItems = false // If this menu will be able to 'receive' items

    // Auto Align options - Start
    var isAutoAlignItems = false
    var autoAlignIgnoreColumns = true
    var autoAlignSkipLines: List<Int> = emptyList()
    private val autoAlignPerPage: Int
        get() {
            var amount = lineAmount * 9
            if (!isAutoAlignItems) return amount
            for (n in 1..autoAlignSkipLines.size) amount -= 9
            if (autoAlignIgnoreColumns) amount -= (lineAmount - autoAlignSkipLines.size) * 2
            return amount
        }
    // Auto Align options - End

    // Back and Next Page buttons options - Start
    private var backPageButtonItem = MineItem(Material.ARROW).name(MenuSystem.BACK_PAGE_BUTTON_DEFAULT_NAME)
    private var backPageButtonPosX = 1
    private var backPageButtonPosY = 1
    private var nextPageButtonItem = MineItem(Material.ARROW).name(MenuSystem.NEXT_PAGE_BUTTON_DEFAULT_NAME)
    private var nextPageButtonPosX = 9
    private var nextPageButtonPosY = 1
    // Back and Next Page buttons options - End
    // Menu Properties - End

    val buttonsToRegister = mutableListOf<MenuButton>()
    private val pages = mutableMapOf<Player, MutableList<MenuPage>>()

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
        if (lineAmount < 1 || lineAmount > 6) error("Menu lineAmount must be between 1 and 6.")
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

    /* // Legacy
    /**
     * Remove all buttons from the menu. You should use it only inside [update] fun before register buttons.
     *
     * @param player the player (holder) of the menu to remove the buttons.
     */
    fun removeAllButtons(player: Player) {
        pages[player]?.let { pages -> pages.forEach { it.buttons.clear() } }
    }
     */

    /**
     * Here all buttons and menu code will be done.
     *
     * @param player the player that will see this menu. All menus are per-player.
     */
    open fun update(player: Player) {
        // Do something
    }

    /**
     * This will be run when this menu is open for a player.
     *
     * IMPORTANT: This will be called after [update]!
     *
     * @param player the player this menu is being opened.
     */
    open fun onOpen(player: Player) {
        // Do something
    }

    /**
     * This will be run when the viewer of the menu([player]) closes the menu.
     *
     * @param player the player that was seeing this menu.
     */
    open fun onClose(player: Player) {
        // Do something
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
    fun open(player: Player, pageToOpen: Int = 1, update: Boolean = false): Inventory {
        update(player) // Rebuilds menu
        if (lineAmount !in 1..6) error("Menu lineAmount must be between 1 and 6.")
        if (!isAutoAlignItems && pageToOpen != 1) error("Cannot open a non-autoAlignItems menu with a page different than 1.")
        autoAlignSkipLines.forEach {
            if (it !in 1..6) error("Menu autoAlignSkipLines can't contains any Int different from 1, 2, 3, 4, 5 and 6.")
            if (it > lineAmount) error("This menu just have $lineAmount lines, can't apply rule to skip line ${it}.")
        }
        val playerPages = pages.getOrPut(player) { mutableListOf() }
        if (!update) {
            playerPages.clear()
        } else {
            playerPages.forEach { p ->
                // p.inventory?.clear()
                p.buttons.filterIsInstance<MenuAnimatedButton>().forEach { b ->
                    b.runAnimationTask?.cancel()
                }
                p.buttons.clear()
                p.backPage = null
                p.nextPage = null
            }
        }

        if (isAutoAlignItems) {
            var lastPage = if (playerPages.isEmpty()) {
                val lp = MenuPage()
                lp.pageId = 1
                lp.inventory = Bukkit.createInventory(
                    null,
                    9 * lineAmount,
                    title.replace("%page%", "${lp.pageId}", true)
                )
                lp.menu = this@MineMenu
                playerPages.add(lp)
                lp
            } else {
                playerPages.first()
            }
            lastPage.inventory!!.clear()

            var lastSlot = 0
            var buttonId = 1
            for (button in buttonsToRegister.filter { !it.fixed }) {
                if (lastPage.buttons.filter { !it.fixed }.size >= autoAlignPerPage) {
                    val newPage = if (playerPages.getOrNull(playerPages.lastIndex + 1) == null) {
                        val np = MenuPage()
                        np.pageId = lastPage.pageId + 1
                        np.inventory = Bukkit.createInventory(
                            null,
                            9 * lineAmount,
                            title.replace("%page%", "${np.pageId}", true)
                        )
                        np.menu = this@MineMenu
                        playerPages.add(np)
                        np
                    } else {
                        playerPages[playerPages.lastIndex + 1]
                    }
                    newPage.backPage = lastPage
                    lastPage.nextPage = newPage
                    lastPage = newPage
                    lastSlot = 0 // reset count
                    buttonId = 1 // reset count
                }
                if (button.icon != null) {
                    if (autoAlignSkipLines.isNotEmpty()) {
                        if (lastSlot == 0) {
                            var lastSkip = 0
                            skip@ for (skip in autoAlignSkipLines) {
                                if ((lastSkip + 1) != skip) break@skip
                                lastSlot += if (autoAlignIgnoreColumns && lastSlot == 1) 10 else 9
                                lastSkip++
                            }
                        }
                    } else if (lastSlot == 0 && autoAlignIgnoreColumns) {
                        lastSlot = 1
                    }
                    if ((lastSlot + 1) < 9 * lineAmount) {
                        lastSlot++
                    }
                    val idToVerify = buttonId - 1
                    if (autoAlignIgnoreColumns && idToVerify != 0 &&
                        idToVerify != 1 &&
                        idToVerify.isMultOf(7) && (lastSlot + 1) < 9 * lineAmount
                    ) {
                        lastSlot += 2
                    }
                    button.autoEffectiveSlot = lastSlot
                    val lastIcon = lastPage.inventory!!.getItem(lastSlot)
                    if (lastIcon == null || lastIcon != button.icon) {
                        lastPage.inventory!!.setItem(lastSlot, button.icon)
                    }
                    lastPage.buttons.removeIf { it.effectiveSlot == lastSlot } // Remove old buttom from there
                    lastPage.buttons.add(button)
                    buttonId++
                }
            }
            playerPages.removeIf { it.pageId != 1 && it.inventory?.contents?.isEmpty() == true }
            for (menuPage in playerPages) {
                val menuPageInv = menuPage.inventory!!
                if (menuPage.hasBackPage) {
                    val backPageId = menuPage.backPage!!.pageId
                    val backPageButton = MenuButton("back-page-${backPageId}")
                    backPageButton.fixed = true
                    backPageButton.positionX = backPageButtonPosX
                    backPageButton.positionY = backPageButtonPosY
                    backPageButton.icon = backPageButtonItem.clone()
                        .name(backPageButtonItem.getName().replace("%page%", "$backPageId", true))
                    backPageButton.click = click@{
                        player.soundClick(2f, 2f)
                        open(player, backPageId)
                    }
                    backPageButton.inventories.add(menuPageInv)
                    menuPage.buttons.removeIf { it.effectiveSlot == backPageButton.effectiveSlot } // Remove old buttom from there
                    menuPage.buttons.add(backPageButton)
                    val lastBackPageIcon = menuPageInv.getItem(backPageButton.effectiveSlot)
                    if (lastBackPageIcon == null || lastBackPageIcon != backPageButton.icon) {
                        menuPageInv.setItem(backPageButton.effectiveSlot, backPageButton.icon)
                    }
                }
                if (menuPage.hasNextPage) {
                    val nextPageId = menuPage.nextPage!!.pageId
                    val nextPageButton = MenuButton("next-page-${nextPageId}")
                    nextPageButton.fixed = true
                    nextPageButton.positionX = nextPageButtonPosX
                    nextPageButton.positionY = nextPageButtonPosY
                    nextPageButton.icon = nextPageButtonItem.clone()
                        .name(nextPageButtonItem.getName().replace("%page%", "$nextPageId", true))
                    nextPageButton.click = click@{
                        player.soundClick(2f, 2f)
                        open(player, nextPageId)
                    }
                    nextPageButton.inventories.add(menuPageInv)
                    menuPage.buttons.removeIf { it.effectiveSlot == nextPageButton.effectiveSlot } // Remove old buttom from there
                    menuPage.buttons.add(nextPageButton)
                    val lastNextPageIcon = menuPageInv.getItem(nextPageButton.effectiveSlot)
                    if (lastNextPageIcon == null || lastNextPageIcon != nextPageButton.icon) {
                        menuPageInv.setItem(nextPageButton.effectiveSlot, nextPageButton.icon)
                    }
                }

                for (fixedButton in buttonsToRegister.filter { it.fixed }) {
                    val lastFixedIcon = menuPageInv.getItem(fixedButton.effectiveSlot)
                    if (lastFixedIcon == null || lastFixedIcon != fixedButton.icon) {
                        menuPageInv.setItem(fixedButton.effectiveSlot, fixedButton.icon)
                    }
                    fixedButton.inventories.add(menuPageInv)
                    menuPage.buttons.removeIf { it.effectiveSlot == fixedButton.effectiveSlot } // Remove old buttom from there
                    menuPage.buttons.add(fixedButton)
                }

                for (button in menuPage.buttons) {
                    if (button is MenuAnimatedButton) {
                        var lastId = 0
                        button.runAnimationTask = utilsMain.syncTimer(0, button.changeFrameDelay) {
                            button.inventories.forEach { buttonInv ->
                                if (buttonInv.viewers.isEmpty()) return@forEach // The inventory have no players seeing it
                                if (lastId + 1 > button.frames.size) {
                                    lastId = 0
                                }
                                button.icon = button.frames[lastId]
                                lastId++

                                val currentAutoSlot = button.autoEffectiveSlot
                                if (currentAutoSlot != null) {
                                    buttonInv.setItem(currentAutoSlot, button.icon)
                                    player.updateInventory()
                                } else {
                                    buttonInv.setItem(button.effectiveSlot, button.icon)
                                    player.updateInventory()
                                }
                            }
                            if (button.inventories.all { it.viewers.isEmpty() }) {
                                button.runAnimationTask?.cancel()
                                button.runAnimationTask = null
                            }
                        }
                    }
                }
            }
            buttonsToRegister.clear()
            playerPages.forEach { p ->
                val toRemove = mutableListOf<ItemStack>()
                p.inventory?.contents?.forEach { i ->
                    if (i == null) return@forEach
                    if (buttons[player]?.none { it.icon == i } == true) {
                        toRemove.add(i)
                    }
                }
                p.inventory?.removeItem(*toRemove.toTypedArray())
            }
            val finalPageToOpen = playerPages.firstOrNull { it.pageId == pageToOpen }
                ?: error("Cannot open page $pageToOpen; Pages size: ${playerPages.size}.")
            val finalPageInv = finalPageToOpen.inventory!!
            player.openInventory(finalPageInv)
            player.openedMineMenu = this@MineMenu
            player.openedMineMenuPage = finalPageToOpen
            onOpen(player)
            MineMenuOpenEvent(
                player,
                this@MineMenu,
                finalPageToOpen
            ).mineCallEvent() // Calls this event
            return finalPageInv
        } else {
            val singlePage = if (playerPages.isEmpty()) {
                val sp = MenuPage()
                sp.pageId = 1
                sp.inventory = Bukkit.createInventory(
                    null,
                    9 * lineAmount,
                    title.replace("%page%", "${sp.pageId}", true)
                )
                sp.menu = this@MineMenu
                playerPages.add(sp)
                sp
            } else {
                playerPages.first()
            }
            singlePage.inventory!!.clear()

            val pageInv = singlePage.inventory!!
            for (button in buttonsToRegister) {
                button.inventories.add(singlePage.inventory!!) // pageInventory
                if (button.icon != null) {
                    val lastIcon = pageInv.getItem(button.effectiveSlot)
                    if (lastIcon == null || lastIcon != button.icon) {
                        pageInv.setItem(button.effectiveSlot, button.icon)
                    }
                    if (button is MenuAnimatedButton) {
                        var lastId = 0
                        button.runAnimationTask = utilsMain.syncTimer(0, button.changeFrameDelay) {
                            button.inventories.forEach { buttonInv ->
                                if (buttonInv.viewers.isEmpty()) return@forEach // The inventory have no players seeing it
                                if (lastId + 1 > button.frames.size) {
                                    lastId = 0
                                }
                                button.icon = button.frames[lastId]
                                lastId++

                                val currentAutoSlot = button.autoEffectiveSlot
                                if (currentAutoSlot != null) {
                                    buttonInv.setItem(currentAutoSlot, button.icon)
                                    player.updateInventory()
                                } else {
                                    buttonInv.setItem(button.effectiveSlot, button.icon)
                                    player.updateInventory()
                                }
                            }
                            if (button.inventories.all { it.viewers.isEmpty() }) {
                                button.runAnimationTask?.cancel()
                                button.runAnimationTask = null
                            }
                        }
                    }
                    singlePage.buttons.removeIf { it.effectiveSlot == button.effectiveSlot } // Remove old buttom from there
                    singlePage.buttons.add(button)
                }
            }
            buttonsToRegister.clear()
            playerPages.forEach { p ->
                val toRemove = mutableListOf<ItemStack>()
                p.inventory?.contents?.forEach { i ->
                    if (i == null) return@forEach
                    if (buttons[player]?.none { it.icon == i } == true) {
                        toRemove.add(i)
                    }
                }
                p.inventory?.removeItem(*toRemove.toTypedArray())
            }
            player.openInventory(pageInv)
            player.openedMineMenu = this@MineMenu
            player.openedMineMenuPage = singlePage
            onOpen(player)
            MineMenuOpenEvent(
                player,
                this@MineMenu,
                singlePage
            ).mineCallEvent() // Calls this event
            return pageInv
        }
    }

    /**
     * Sets up the following menu properties:
     * - [backPageButtonItem]
     * - [backPageButtonPosX]
     * - [backPageButtonPosY]
     *
     * IMPORTANT: DON'T use this function inside a non-[isAutoAlignItems] - [MineMenu], or this will throw an [IllegalStateException] error.
     *
     * @param item the new [MineItem] to be set as [backPageButtonItem]. Use '%page%' in its name as replacer if you want.
     * @param x the new [Int] to be set as [backPageButtonPosX]. Default: 1.
     * @param y the new [Int] to be set as [backPageButtonPosY]. Default: 1.
     * @throws IllegalStateException If [isAutoAlignItems] of this [MineMenu] is false.
     */
    fun setupBackButton(item: MineItem = backPageButtonItem, x: Int = 1, y: Int = 1) {
        backPageButtonItem = item.clone()
        backPageButtonPosX = x
        backPageButtonPosY = y
    }

    /**
     * Sets up the following menu properties:
     * - [nextPageButtonItem]
     * - [nextPageButtonPosX]
     * - [nextPageButtonPosY]
     *
     * IMPORTANT: DON'T use this function inside a non-[isAutoAlignItems] - [MineMenu], or this will throw an [IllegalStateException] error.
     *
     * @param item the new [MineItem] to be set as [nextPageButtonItem]. Use '%page%' in its name as replacer if you want.
     * @param x the new [Int] to be set as [nextPageButtonPosX]. Default: 9.
     * @param y the new [Int] to be set as [nextPageButtonPosY]. Default: 1.
     * @throws IllegalStateException If [isAutoAlignItems] of this [MineMenu] is false.
     */
    fun setupNextButton(item: MineItem = nextPageButtonItem, x: Int = 9, y: Int = 1) {
        nextPageButtonItem = item.clone()
        nextPageButtonPosX = x
        nextPageButtonPosY = y
    }

    /**
     * Creates a new button inside the menu.
     *
     * IMPORTANT: if the param [x] and [y] is not set, the button will automatically
     * be an auto-align button. Otherwise, the button will follow the given [x] and [y]
     * ignoring the auto align buttons 'option'.
     *
     * @param buttonName the button name. Can be null.
     * @param x the icon X position. Default: null. (If null the button will be auto align)
     * @param y the icon Y position. Default: null. (If null the button will be auto align)
     * @param setup the [MenuButton] builder.
     * @return the built [MenuButton].
     */
    fun button(
        buttonName: String? = null,
        x: Int? = null,
        y: Int? = null,
        setup: (MenuButton.() -> Unit)
    ): MenuButton {
        val newButton = if (buttonName != null) MenuButton(buttonName) else MenuButton()
        newButton.setup()
        if (x != null && y != null) {
            newButton.fixed = true
            newButton.positionX = x
            newButton.positionY = y
        } else {
            newButton.fixed = false
        }
        buttonsToRegister.add(newButton)
        return newButton
    }

    /**
     * Creates a new [MenuAnimatedButton] inside the menu.
     *
     * IMPORTANT: if the param [x] and [y] is not set, the button will automatically
     * be an auto-align button. Otherwise, the button will follow the given [x] and [y]
     * ignoring the auto align buttons 'option'.
     *
     * @param buttonName the button name. Can be null.
     * @param x the icon X position. Default: null. (If null the button will be auto align)
     * @param y the icon Y position. Default: null. (If null the button will be auto align)
     * @param changeFrameDelay the time to change between icons (frames). Defined in ticks (20 = 1s).
     * @param setup the [MenuAnimatedButton] builder.
     * @return the built [MenuAnimatedButton].
     * @throws IllegalStateException if [changeFrameDelay] is less than 1.
     */
    fun animatedButton(
        buttonName: String? = null,
        x: Int? = null,
        y: Int? = null,
        changeFrameDelay: Long = 20,
        setup: (MenuAnimatedButton.() -> Unit)
    ): MenuAnimatedButton {
        if (changeFrameDelay < 1) error("MenuAnimatedButton property 'changeFrameDelay' cannot be less than 1.")
        val newAnimatedButton = if (buttonName != null) MenuAnimatedButton(buttonName) else MenuAnimatedButton()
        newAnimatedButton.setup()
        if (x != null && y != null) {
            newAnimatedButton.fixed = true
            newAnimatedButton.positionX = x
            newAnimatedButton.positionY = y
        } else {
            newAnimatedButton.fixed = false
        }
        newAnimatedButton.changeFrameDelay = changeFrameDelay
        buttonsToRegister.add(newAnimatedButton)
        return newAnimatedButton
    }

    /**
     * Creates a new [MenuButton] inside this menu with will be built using async.
     *
     * IMPORTANT: if the param [x] and [y] is not set, the button will automatically
     * be an auto-align button. Otherwise, the button will follow the given [x] and [y]
     * ignoring the auto align buttons 'option'.
     *
     * @param buttonName the button name. Can be null.
     * @param x the icon X position. Default: null. (If null the button will be auto align)
     * @param y the icon Y position. Default: null. (If null the button will be auto align)
     * @param setup the [MenuButton] builder.
     * @return the built [MenuButton]. This button will be changed once [setup] is complete run using async.
     * @see [BukkitTimeHandler.asyncDelay]
     * @see [BukkitTimeHandler.syncTask]
     */
    fun asyncButton(
        buttonName: String? = null,
        x: Int? = null,
        y: Int? = null,
        setup: (MenuButton.() -> Unit)
    ): MenuButton {
        val newButton = if (buttonName != null) MenuButton(buttonName) else MenuButton()
        newButton.icon = MineItem(Material.STAINED_GLASS_PANE).data(7).name(MenuSystem.ASYNC_BUTTON_LOADING_NAME)
        if (x != null && y != null) {
            newButton.fixed = true
            newButton.positionX = x
            newButton.positionY = y
        } else {
            newButton.fixed = false
        }
        buttonsToRegister.add(newButton)
        utilsMain.asyncDelay(1) {
            try {
                newButton.setup()
                utilsMain.syncTask {
                    try {
                        newButton.inventories?.forEach { buttonInv ->
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

    // Menu Listeners - Start
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInvClose(e: InventoryCloseEvent) { // PlayerQuitEvent is not used anymore, onInvClose already clear all lists on quit.
        if (e.player !is Player) return
        val player = e.player as Player
        player.runBlock {
            // if (player.openedMineMenu != this) return@runBlock // Useless ?
            val playerPages = pages[player] ?: return@runBlock
            if (playerPages.none { e.inventory == it.inventory!! }) return@runBlock
            /*
            if (playerPages.any { it.nextPage != null && it.nextPage!!.inventory == player.openInventory }) {
                // pages.remove(player)
                return@runBlock
            }
             */
            player.openedMineMenu = null
            player.openedMineMenuPage = null
            onClose(player)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onInvClick(e: InventoryClickEvent) {
        val player = e.player
        player.runBlock {
            if (e.clickedInventory == null) return@runBlock
            if (player.openedMineMenu != this) return@runBlock
            val playerPages = pages[player] ?: return@runBlock
            if (this.canReceiveItems && !e.isShiftClick && e.rawSlot > this.lineAmount * 9 - 1) return@runBlock
            val clickedPage = playerPages.firstOrNull { e.clickedInventory == it.inventory!! }
            val clickedButton = clickedPage?.buttons?.firstOrNull { e.slot == it.effectiveSlot }
            player.runBlock {
                clickedButton?.click?.invoke(e)
            }
            e.isCancelled = true
        }
    }
    // Menu Listeners - End
}