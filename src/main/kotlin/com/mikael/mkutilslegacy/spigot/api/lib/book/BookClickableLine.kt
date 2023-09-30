package com.mikael.mkutilslegacy.spigot.api.lib.book

import com.mikael.mkutilslegacy.api.toTextComponent
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent

@Deprecated("Broken")
@Suppress("WARNINGS")
open class BookClickableLine(vararg component: BaseComponent) : TextComponent(*component) {

    constructor(textLine: String) : this(*textLine.toTextComponent())

    var textLine: String
        get() = this.text
        set(newLine) {
            this.text = newLine
        }

    // Click Event (ClickEvent)

    val clickAction: ClickEvent.Action get() = this.clickEvent.action

    fun setClickAction(action: ClickEvent.Action, value: String) {
        this.clickEvent = ClickEvent(action, value)
    }

    // Hover Event (HoverEvent)

    val hoverAction: HoverEvent.Action get() = this.hoverEvent.action

    fun setHoverAction(action: HoverEvent.Action, vararg value: String) {
        this.hoverEvent = HoverEvent(action, value.joinToString("\n").toTextComponent())
    }

}