// src/main/kotlin/com/example/waypointTp/ui/OpenMenuItemListener.kt
package com.example.waypointTp.ui

import com.example.waypointTp.repo.YamlWaypointRepository
import com.example.waypointTp.util.Keys
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class OpenMenuItemListener(
    private val plugin: JavaPlugin,
    private val repo: YamlWaypointRepository
) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onInteract(e: PlayerInteractEvent) {
        // 両手で二重発火するため、メインハンド以外は無視
        if (e.hand != EquipmentSlot.HAND) return

        // 右クリックのみ処理
        val a = e.action
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return

        val item = e.item ?: return
        val meta = item.itemMeta ?: return

        // PDCタグで専用アイテムか判定
        if (!meta.persistentDataContainer.has(Keys.WP_OPENER, PersistentDataType.BYTE)) return

        // ブロック右クリック等の既定動作はキャンセル
        e.isCancelled = true

        // 次のTickで安全にGUIを開く
        object : BukkitRunnable() {
            override fun run() {
                WaypointMenu(repo).open(e.player, 0)
            }
        }.runTask(plugin)
    }

    companion object {
        fun createOpenerItem(): ItemStack {
            val it = ItemStack(Material.COMPASS)
            val meta = it.itemMeta
            meta.displayName(Component.text("§eウェイポイントを開く"))
            meta.persistentDataContainer.set(Keys.WP_OPENER, PersistentDataType.BYTE, 1.toByte())
            it.itemMeta = meta
            return it
        }
    }
}
