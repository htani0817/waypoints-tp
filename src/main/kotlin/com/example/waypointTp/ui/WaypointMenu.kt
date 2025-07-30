package com.example.waypointTp.ui

import com.example.waypointTp.model.Waypoint
import com.example.waypointTp.repo.YamlWaypointRepository
import com.example.waypointTp.util.Keys
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class WaypointMenuHolder(val page: Int = 0) : InventoryHolder {
    private var inv: Inventory? = null
    override fun getInventory(): Inventory =
        inv ?: Bukkit.createInventory(this, 54, Component.text("Waypoints")).also { inv = it }
}

class WaypointMenu(private val repo: YamlWaypointRepository) {
    fun open(p: Player, page: Int = 0) {
        val holder = WaypointMenuHolder(page)
        val inv = holder.inventory
        inv.clear()

        val list = repo.all()
        val pageSize = 45
        val start = page * pageSize
        val end = (start + pageSize).coerceAtMost(list.size)

        for ((slot, wp) in list.subList(start, end).withIndex()) {
            inv.setItem(slot, toItem(wp))
        }

        // 追加ボタン（53番スロット）
        val add = ItemStack(Material.LODESTONE).apply {
            itemMeta = itemMeta.applyName("📍 現在地を登録")
        }
        inv.setItem(53, add)

        p.openInventory(inv)
    }

    private fun ItemMeta.applyName(name: String): ItemMeta {
        displayName(Component.text(name))
        return this
    }

    private fun toItem(wp: Waypoint): ItemStack {
        val it = ItemStack(Material.COMPASS)
        val meta = it.itemMeta
        meta.displayName(Component.text("§e${wp.name}"))
        meta.lore(listOf(
            Component.text("§7x:${"%.1f".format(wp.x)} y:${"%.1f".format(wp.y)} z:${"%.1f".format(wp.z)}")
        ))
        meta.persistentDataContainer.set(Keys.WP_ID, PersistentDataType.STRING, wp.id.toString())
        it.itemMeta = meta
        return it
    }

    companion object {
        fun extractId(meta: ItemMeta?): UUID? {
            val idStr = meta?.persistentDataContainer?.get(Keys.WP_ID, PersistentDataType.STRING) ?: return null
            return runCatching { UUID.fromString(idStr) }.getOrNull()
        }
    }
}
