package com.mikael.mkutilslegacy.spigot.api.lib.book

import com.mikael.mkutilslegacy.api.toTextComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent

@Deprecated("Not done yet.")
open class BookClickableLine(val component: TextComponent) {

    constructor(textLine: String) : this(textLine.toTextComponent())

    var textLine: String
        get() = component.text
        set(newLine) {
            component.text = newLine
        }

    val clickAction: ClickEvent.Action get() = component.clickEvent.action

    val hoverAction: HoverEvent.Action get() = component.hoverEvent.action

}