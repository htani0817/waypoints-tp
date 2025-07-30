// src/main/kotlin/com/example/waypointTp/ui/OpenMenuItemListener.kt
package com.example.waypointTp.ui

import com.example.waypointTp.repo.YamlWaypointRepository
import com.example.waypointTp.util.Keys
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class OpenMenuItemListener(
    private val plugin: JavaPlugin,
    private val repo: YamlWaypointRepository
) : Listener {

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        if (e.hand != EquipmentSlot.HAND) return // オフハンド重複防止
        val p: Player = e.player
        val item = e.item ?: return
        val meta = item.itemMeta ?: return
        val tag = meta.persistentDataContainer.get(Keys.WP_OPENER, PersistentDataType.BYTE) ?: return
        if (tag.toInt() != 1) return

        e.isCancelled = true
        WaypointMenu(repo).open(p, 0)
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
