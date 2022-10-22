package com.mikael.mkutilslegacy.spigot.api.lib.menu.example

import com.mikael.mkutilslegacy.spigot.api.lib.MineItem
import com.mikael.mkutilslegacy.spigot.api.lib.menu.MenuButton
import com.mikael.mkutilslegacy.spigot.api.lib.menu.MineMenu
import com.mikael.mkutilslegacy.spigot.api.soundPling
import net.eduard.api.lib.kotlin.formatDate
import net.eduard.api.lib.kotlin.formatHour
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SinglePageExampleMenu : MineMenu("Cool Example Menu :D", 6) {

    /**
     * Here, you'll learn how to create a single-page [MineMenu].
     *
     * @author Mikael
     * @see MineMenu
     */

    /**
     * Let's start creating a companion object.
     * Inside it, let's create the menu [instance].
     *
     * As you can see:
     * - lateinit var [instance]: [SinglePageExampleMenu]
     *
     * Then, let's create or init.
     * Inside it, let's define the instance value.
     *
     * As you can see:
     * - [instance] = [this@SinglePageExampleMenu]
     *
     * We'll use the menu [instance] to open it to a player.
     * Using [SinglePageExampleMenu.instance].open(player)
     *
     * Another values of the menu can be set here inside the init.
     * For example, if this menu [isAutoUpdate].
     */
    companion object {
        lateinit var instance: SinglePageExampleMenu
    }

    init {
        instance = this@SinglePageExampleMenu

        isAutoUpdate = true // This menu will auto update, if on mkUtils config this is set to True.
    }

    /**
     * Now, let's create the [update] fun.
     * Or better, override it.
     *
     * @param [player] the player that is seeing this menu.
     *
     * @see removeAllButtons
     * @see button
     */
    override fun update(player: Player) {
        /**
         * Always start with [removeAllButtons]
         */
        removeAllButtons(player)

        /**
         * Now, let's create a [button].
         * The buttonName can be set how you want. Don't use spaces. use - instead.
         */
        button("info-paper") {
            /**
             * Here, you'll set the menu button position.
             * Using X and Y.
             */
            setPosition(5, 3)

            /**
             * Here, you'll set the menu button icon.
             * The menu icon is an [ItemStack].
             * As you are using mkUtils, by preference, use [MineItem].
             *
             * @see ItemStack
             * @see MineItem
             */
            icon = MineItem(Material.PAPER)
                .name("§aRandom infos (For tests)")
                .lore(
                    "",
                    "§fCurrent time: §e${System.currentTimeMillis().formatDate()} ${
                        System.currentTimeMillis().formatHour()
                    }"
                )

            /**
             * Here, you'll set the button click. (ClickEffect)
             * See more details here: [MenuButton]
             *
             * Important: Inside a click, IT is InventoryClickEvent.
             * You DON'T need to mark it.isCancelled to True.
             * The click on a menu button will always be cancelled, and the button will stay on the same slot.
             */
            click = infoClick@{
                player.soundPling()
                player.sendMessage("You're the player: ${player.name}")
                if (player.isFlying) {
                    return@infoClick // Return can be used here if needed, since you define a name for your click. Example: click = clickName@ { ... }
                }
                player.sendMessage("You're not flying :c")
            }
        }
    }

}