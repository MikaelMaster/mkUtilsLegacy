package com.mikael.mkutilslegacy.spigot.api.lib.book

import com.mikael.mkutilslegacy.api.toTextComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent

@Suppress("WARNINGS")
open class BookClickableLine(text: String) : TextComponent(*TextComponent.fromLegacyText(text)) {

    var textLine: String
        get() = text
        set(newLine) {
            this.text = newLine
        }

    // Click Action (ClickEvent)
    val clickAction: ClickEvent.Action get() = this.clickEvent.action
    fun setClickAction(action: ClickEvent.Action, value: String) {
        this.clickEvent = ClickEvent(action, value)
    }

    // Hover Action (HoverEvent)
    val hoverAction: HoverEvent.Action get() = this.hoverEvent.action
    fun setHoverAction(action: HoverEvent.Action, vararg value: String) {
        this.hoverEvent = HoverEvent(action, arrayOf(value.joinToString("\n").toTextComponent()))
    }

}