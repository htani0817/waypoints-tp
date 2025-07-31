package com.example.waypointTp.ui

import com.example.waypointTp.i18n.Messages
import com.example.waypointTp.repo.YamlWaypointRepository
import com.example.waypointTp.ui.WaypointMenuHolder
import com.example.waypointTp.util.Keys
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class MenuListener(
    private val plugin: JavaPlugin,
    private val repo: YamlWaypointRepository,
    private val messages: Messages,
    private val namePrompt: AnvilNamePrompt   // 金床入力
) : Listener {

    private val pendingDelete = mutableMapOf<UUID, UUID>() // playerUUID -> waypointId

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val holder = e.inventory.holder
        if (holder !is WaypointMenuHolder) return
        e.isCancelled = true

        val p = e.whoClicked as? Player ?: return
        val item = e.currentItem ?: return

        // 自作GUI以外（プレイヤーインベントリ側）のクリックは無視
        if (e.clickedInventory != e.view.topInventory) return

        // ◀ 前へ（45）
        if (e.slot == 45 && item.type == Material.ARROW) {
            object : BukkitRunnable() {
                override fun run() { WaypointMenu(repo).open(p, holder.page - 1) }
            }.runTask(plugin)
            return
        }

        // 📍 追加（49）→ 金床で名前入力 → 保存
        if (e.slot == 49 && item.type == Material.LODESTONE) {
            val loc = p.location
            val suggested = "wp-${System.currentTimeMillis()}"
            p.closeInventory()
            namePrompt.open(p, suggested) { typed ->
                if (typed.isNullOrBlank()) {
                    object : BukkitRunnable() {
                        override fun run() { WaypointMenu(repo).open(p, holder.page) }
                    }.runTask(plugin)
                    return@open
                }
                val id = UUID.randomUUID()
                repo.saveById(
                    id = id, name = typed,
                    world = loc.world!!.uid, x = loc.x, y = loc.y, z = loc.z,
                    yaw = loc.yaw, pitch = loc.pitch, creator = p.uniqueId
                )
                p.sendMessage(messages.text("saved", mapOf("name" to typed)))
                object : BukkitRunnable() {
                    override fun run() { WaypointMenu(repo).open(p, holder.page) }
                }.runTask(plugin)
            }
            return
        }

        // ▶ 次へ（53）
        if (e.slot == 53 && item.type == Material.ARROW) {
            object : BukkitRunnable() {
                override fun run() { WaypointMenu(repo).open(p, holder.page + 1) }
            }.runTask(plugin)
            return
        }

        // --- Waypoint アイテム（PDCにIDを持たせている） ---
        val meta = item.itemMeta ?: return
        val idStr = meta.persistentDataContainer.get(Keys.WP_ID, PersistentDataType.STRING) ?: return
        val id = runCatching { UUID.fromString(idStr) }.getOrNull() ?: return

        when (e.click) {
            ClickType.LEFT -> {
                val wp = repo.find(id) ?: return
                val loc = repo.toLocation(wp) ?: return
                p.sendMessage(messages.text("teleporting", mapOf("name" to wp.name)))
                p.teleportAsync(loc).thenAccept { ok ->
                    if (!ok) p.sendMessage(messages.text("teleport_failed"))
                }
            }
            ClickType.RIGHT -> {
                if (pendingDelete[p.uniqueId] == id) {
                    repo.delete(id)
                    pendingDelete.remove(p.uniqueId)
                    p.sendMessage(messages.text("deleted"))
                    object : BukkitRunnable() {
                        override fun run() { WaypointMenu(repo).open(p, holder.page) } // open側でclamp
                    }.runTask(plugin)
                } else {
                    pendingDelete[p.uniqueId] = id
                    p.sendMessage(messages.text("delete_confirm"))
                    // 10秒（200tick）で保留解除
                    plugin.server.scheduler.runTaskLater(plugin, Runnable {
                        if (pendingDelete[p.uniqueId] == id) pendingDelete.remove(p.uniqueId)
                    }, 10 * 20L)
                }
            }
            else -> {}
        }
    }
}
