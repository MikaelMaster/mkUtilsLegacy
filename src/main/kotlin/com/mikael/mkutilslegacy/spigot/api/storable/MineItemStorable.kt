package com.mikael.mkutilslegacy.spigot.api.storable

import com.mikael.mkutilslegacy.spigot.api.lib.MineItem
import net.eduard.api.lib.storage.Storable
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag

class MineItemStorable : Storable<MineItem> { // This is not completed yet, but works

    override fun store(map: MutableMap<String, Any>, item: MineItem) {
        map["material"] = item.type.name
        map["amount"] = item.amount
        map["data"] = item.durability.toInt()
        map["name"] = item.getName()
        map["lore"] = item.getLore()
        map["flags"] = item.itemMeta.itemFlags.map { it.name }
        map["glowed"] = item.isGlowed
        map["enchants"] = item.enchantments.mapKeys { it.key.name }
        val skinUrl = item.getSkinURL()
        skinUrl?.let {
            map["skinUrl"] = it
        }
        val skullName = item.getSkullName()
        skullName?.let {
            map["skinUrl"] = it
        }
        // map["data"] = item.toBase64()
    }

    @Suppress("UNCHECKED_CAST")
    override fun restore(map: MutableMap<String, Any>): MineItem {
        val item = MineItem(Material.valueOf(map["material"].toString()), map["amount"].toString().toInt())
        item.data(map["data"].toString().toInt())
        item.name(map["name"].toString())
        item.lore(map["lore"] as List<String>)
        for (flag in map["flags"] as List<String>) {
            item.addFlags(ItemFlag.valueOf(flag))
        }
        item.glowed(map["glowed"].toString().toBooleanStrict())
        for (enchant in map["enchants"] as Map<String, Int>) {
            item.addEnchant(Enchantment.getByName(enchant.key), enchant.value)
        }
        val skinUrl = map["skinUrl"]
        if (skinUrl != null) {
            item.skin(skinUrl.toString())
        }
        val skullName = map["skullName"]
        if (skinUrl != null) {
            item.skull(skullName.toString())
        }
        return item
        // return MineItem.fromBase64(map["data"] as String) ?: MineItem(Material.BARRIER)
    }
}