package com.mikael.mkutilslegacy.spigot.api.lib

import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.ItemMeta

/**
 * [MineBook] util class
 *
 * This class extends a [MineItem].
 * This class uses [net.minecraft.server.v1_8_R3]! (NMS 1.8_R3)
 *
 * To create/invoke a new MineBook you can use:
 * - MineBook() -> A new [MineItem] with the material [Material.WRITTEN_BOOK] will be given as 'baseItem'.
 * - MineBook(baseItem: [MineItem]) -> This will keep the 'baseItem' [ItemMeta] and properties.
 * - [MineItem.toMineBook] -> This will use the given [MineItem] as 'baseItem'.
 *
 * @author Mikael
 * @see MineItem
 * @see CraftItemStack
 * @see BookMeta
 */
open class MineBook(baseItem: MineItem) : MineItem(baseItem) {

    /**
     * @return [MineBook] giving an 'empty' [MineItem] as 'baseItem' param.
     */
    constructor() : this(MineItem(Material.WRITTEN_BOOK))

    /**
     * @return this ([MineItem.getItemMeta]) as [BookMeta].
     */
    val book get() = this.itemMeta as BookMeta

    /**
     * Sets/returns the [book] title.
     * @see BookMeta.hasTitle
     * @see BookMeta.setTitle
     * @see BookMeta.getTitle
     */
    var title
        get() = book.title
        set(newTitle) {
            book.title = newTitle
            this.itemMeta = book
        }

    /**
     * Sets/returns the [book] author.
     * @see BookMeta.hasAuthor
     * @see BookMeta.getAuthor
     * @see BookMeta.setAuthor
     */
    var visualAuthor
        get() = book.author
        set(newAuthor) {
            book.author = newAuthor
            this.itemMeta = book
        }

    /**
     * @see [BookMeta.getPages]
     */
    val pages get() = book.pages

    /**
     * @see [BookMeta.getPageCount]
     */
    val pageCount get() = book.pageCount

    fun setTitle(title: String): MineBook {
        this.title = title
        this.itemMeta = book
        return this
    }

    /**
     * @param pages the new pages to set on this book. Each page can have multiple [String] lines.
     * @see pages
     * @see pageCount
     */
    fun setPages(vararg pages: List<String>) {
        book.pages.clear()
        for ((pageId, page) in pages.withIndex()) {
            page.forEach { pageLine ->
                book.setPage(pageId, pageLine)
            }
        }
    }

    /**
     * Note: This function uses NMS. (NMS 1.8_R3)
     *
     * @param player the player to open this book gui.
     * @return this [MineBook].
     * @see CraftPlayer // NMS
     * @see CraftItemStack.asNMSCopy // NMS
     */
    // @suppress because of [player] as [CraftPlayer]. (NMS of this function)
    // @SuppressWarnings("unchecked") // Useless?
    fun open(player: Player): MineBook {
        val invItem = player.inventory.getItem(0)
        player.inventory.setItem(0, this) // This is necessary, otherwise the book gui will not open
        (player as CraftPlayer).handle.openBook(CraftItemStack.asNMSCopy(this)) // Open book gui
        player.inventory.setItem(0, invItem)
        return this
    }

}