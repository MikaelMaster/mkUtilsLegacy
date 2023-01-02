package com.mikael.mkutilslegacy.api.lib

import com.mikael.mkutilslegacy.spigot.api.lib.book.MineBook
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent

/**
 * MineSmartText util class
 *
 * This is hybrid; Can be used in Spigot (also Paper) and Bungee (also Waterfall).
 *
 * @param component a [TextComponent] to be this [MineSmartText] itself.
 * @author Mikael
 * @see TextComponent
 */
class MineSmartText(val component: TextComponent) : TextComponent(component) {

    /**
     * @param text a [String] to be the base of the [TextComponent]-- that will be this [MineSmartText] itself.
     */
    constructor(text: String) : this(TextComponent(text))

    /**
     * Sets the [ClickEvent] for this [MineSmartText].
     *
     * Available options for each [ClickEvent.Action] type:
     *
     * * [ClickEvent.Action.OPEN_URL] -> An URL to be opened in player's PC browser;
     * * [ClickEvent.Action.OPEN_FILE] -> *Unknown until this moment*;
     * * [ClickEvent.Action.RUN_COMMAND] -> A [String] (command) to be run by the player (Should start with '/').
     * You can also use this to force the player to send a message, just remove the '/' in the begging of the [toExecute] text;
     * * [ClickEvent.Action.SUGGEST_COMMAND] -> A [String] to be suggested as a command in the player chat field;
     * * [ClickEvent.Action.CHANGE_PAGE] -> Numeric value (1, 2, 3, etc. - [MineBook]).
     *
     * @param action the [ClickEvent.Action] to be executed when the text is clicked.
     * @param toExecute an [String] that will be executed when the text is clicked,
     * following the limitations of the [action] type.
     * @return this [MineSmartText].
     * @see TextComponent.setClickEvent
     * @see ClickEvent
     */
    fun setClickAction(action: ClickEvent.Action, toExecute: String): MineSmartText {
        this.clickEvent = ClickEvent(action, toExecute)
        return this
    }

    /**
     * Sets the [HoverEvent] for this [MineSmartText].
     *
     * @param action the [HoverEvent.Action] to be executed when the mouse hovers this text.
     * @param toShow a [List] of [BaseComponent] to be displayed as the hover,
     * following the limitations of the [action] type.
     * @return this [MineSmartText].
     * @see TextComponent.setHoverEvent
     * @see HoverEvent
     */
    fun setHoverAction(action: HoverEvent.Action, toShow: List<BaseComponent>): MineSmartText {
        this.hoverEvent = HoverEvent(action, toShow.toTypedArray())
        return this
    }

    /**
     * Sets the [text] of this [MineSmartText] as a [List] of [String] using the internal Minecraft separator '\n'.
     *
     * To get the [text] as a [List] of [String] you can use [getTextAsLines].
     *
     * @param lines the [List] of [String] to be set as lines.
     * @see TextComponent.text
     * @see List.joinToString // Using '\n' as internal line break-- Minecraft internal feature.
     */
    fun setTextAsLines(lines: List<String>): MineSmartText {
        this.text = lines.joinToString("\n")
        return this
    }

     /**
     * Returns the [text] as a [List] of [String] using the internal Minecraft separator '/n'.
     *
     * IMPORTANT: Make sure you set the text of this [MineSmartText] using [setTextAsLines], otherwise this may not work well.
     *
     * @return the [text] as a [List] of [String] with all lines set.
     * @see String.split // Using '\n' as internal line break-- Minecraft internal feature.
     */
    fun getTextAsLines(): List<String> {
        return this.text.split("\n")
    }

    /**
     * @return a new [TextComponent] with the properties set using this [MineSmartText].
     */
    fun toTextComponent(): TextComponent {
        return this
    }

}