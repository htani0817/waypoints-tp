package com.example.waypointTp.ui

import com.example.waypointTp.model.Waypoint
import com.example.waypointTp.repo.YamlWaypointRepository
import com.example.waypointTp.util.Keys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import kotlin.math.ceil

class WaypointMenuHolder(val page: Int = 0, val totalPages: Int = 1) : InventoryHolder {
    private var inv: Inventory? = null
    override fun getInventory(): Inventory {
        val title = if (totalPages > 1)
            "Waypoints (${page + 1}/${totalPages})"
        else
            "Waypoints"
        return inv ?: Bukkit.createInventory(this, 54, Component.text(title)).also { inv = it }
    }
}

class WaypointMenu(private val repo: YamlWaypointRepository) {
    private val pageSize = 45 // 0..44 をデータ領域に

    fun open(p: Player, page: Int = 0) {
        val all = repo.all()
        val totalPages = if (all.isEmpty()) 1 else ceil(all.size / pageSize.toDouble()).toInt()
        val clamped = page.coerceIn(0, totalPages - 1)

        val holder = WaypointMenuHolder(clamped, totalPages)
        val inv = holder.inventory
        inv.clear()

        val start = clamped * pageSize
        val end = (start + pageSize).coerceAtMost(all.size)
        for ((slot, wp) in all.subList(start, end).withIndex()) {
            inv.setItem(slot, toItem(wp))
        }

        // ◀ 前へ（45）
        if (clamped > 0) inv.setItem(45, navItem(Material.ARROW, "◀ 前へ"))
        // 📍 追加（49）… 金床入力で名前を付けて保存
        inv.setItem(49, ItemStack(Material.LODESTONE).apply {
            itemMeta = itemMeta.applyName("📍 現在地を登録")
        })
        // ▶ 次へ（53）
        if (clamped < totalPages - 1) inv.setItem(53, navItem(Material.ARROW, "▶ 次へ"))

        p.openInventory(inv)
    }

    private fun navItem(type: Material, name: String) = ItemStack(type).apply {
        itemMeta = itemMeta.applyName(name)
    }

    private fun ItemMeta.applyName(name: String): ItemMeta {
        displayName(Component.text(name))
        return this
    }

    // 作成者名を解決（オンライン名 > 最終既知名 > 短縮UUID）
    private fun resolveCreatorName(uuid: UUID): String {
        Bukkit.getPlayer(uuid)?.let { return it.name }
        val off = Bukkit.getOfflinePlayer(uuid)
        return off.name ?: uuid.toString().substring(0, 8)
    }

    private fun toItem(wp: Waypoint): ItemStack {
        val it = ItemStack(Material.COMPASS)
        val meta = it.itemMeta

        // 表示名（Adventure の色指定）
        meta.displayName(Component.text(wp.name, NamedTextColor.YELLOW))

        // 作成者・座標・ワールドを lore に表示
        val creatorName = resolveCreatorName(wp.creator)
        val worldName = Bukkit.getWorld(wp.world)?.name ?: wp.world.toString().substring(0, 8)
        val lore = buildList<Component> {
            add(Component.text("作成者: ", NamedTextColor.GRAY).append(Component.text(creatorName, NamedTextColor.WHITE)))
            add(
                Component.text("座標: ", NamedTextColor.DARK_GRAY)
                    .append(Component.text("x:${"%.1f".format(wp.x)} y:${"%.1f".format(wp.y)} z:${"%.1f".format(wp.z)}", NamedTextColor.GRAY))
            )
            add(Component.text("ワールド: ", NamedTextColor.DARK_GRAY).append(Component.text(worldName, NamedTextColor.GRAY)))
        }
        meta.lore(lore)

        // PDC で内部IDを埋め込む
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
