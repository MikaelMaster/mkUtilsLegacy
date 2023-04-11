package com.mikael.mkutilslegacy.spigot.api.storable

import com.mikael.mkutilslegacy.spigot.api.lib.MineItem
import net.eduard.api.lib.storage.Storable
import org.bukkit.Material

class MineItemStorable : Storable<MineItem> { // This is not completed yet, but works

    override fun store(map: MutableMap<String, Any>, item: MineItem) {
        map["data"] = item.toBase64()
    }

    @Suppress("UNCHECKED_CAST")
    override fun restore(map: MutableMap<String, Any>): MineItem {

        return MineItem.fromBase64(map["data"] as String)?: MineItem(Material.BARRIER)
    }
}