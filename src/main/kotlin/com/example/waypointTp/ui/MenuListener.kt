package com.example.waypointTp.ui

import com.example.waypointTp.i18n.Messages
import com.example.waypointTp.repo.YamlWaypointRepository
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
    private val namePrompt: AnvilNamePrompt
) : Listener {

    private val pendingDelete = mutableMapOf<UUID, UUID>()

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val holder = e.inventory.holder
        if (holder !is WaypointMenuHolder) return
        e.isCancelled = true

        val p = e.whoClicked as? Player ?: return
        val item = e.currentItem ?: return
        if (e.clickedInventory != e.view.topInventory) return

        /* --- ページナビ & 追加ボタン --- */
        when (e.slot) {
            45 -> if (item.type == Material.ARROW) WaypointMenu(repo).openLater(p, holder.page - 1)
            49 -> if (item.type == Material.LODESTONE) return addWaypointWithAnvil(p, holder.page)
            53 -> if (item.type == Material.ARROW) WaypointMenu(repo).openLater(p, holder.page + 1)
        }

        /* --- Waypointアイテム --- */
        val idStr = item.itemMeta?.persistentDataContainer
            ?.get(Keys.WP_ID, PersistentDataType.STRING) ?: return
        val id = runCatching { UUID.fromString(idStr) }.getOrNull() ?: return

        when (e.click) {
            ClickType.LEFT  -> teleport(p, id)
            ClickType.RIGHT -> deleteWith2Step(p, id, holder.page)
            else -> Unit                           // FIX: when を文として完結させる
        }
    }

    /* ========== 個別処理 ========== */

    private fun deleteWith2Step(p: Player, id: UUID, page: Int) {
        val wp = repo.find(id) ?: return
        val canDelete = wp.creator == p.uniqueId

        if (!canDelete) {
            p.sendMessage(messages.text("delete_not_owner"))
            return
        }

        if (pendingDelete[p.uniqueId] == id) {
            repo.delete(id)
            pendingDelete.remove(p.uniqueId)
            p.sendMessage(messages.text("deleted"))
            WaypointMenu(repo).openLater(p, page)
        } else {
            pendingDelete[p.uniqueId] = id
            p.sendMessage(messages.text("delete_confirm"))
            plugin.server.scheduler.runTaskLater(
                plugin, Runnable { pendingDelete.remove(p.uniqueId, id) }, 10 * 20L
            )
        }
    }

    private fun teleport(p: Player, id: UUID) {
        val wp = repo.find(id) ?: return
        val loc = repo.toLocation(wp) ?: return
        p.sendMessage(messages.text("teleporting", mapOf("name" to wp.name)))
        p.teleportAsync(loc).thenAccept { ok ->
            if (!ok) p.sendMessage(messages.text("teleport_failed"))
        }
    }

    private fun addWaypointWithAnvil(p: Player, page: Int) {
        val loc = p.location
        val suggested = "wp-${System.currentTimeMillis()}"
        p.closeInventory()

        namePrompt.open(p, suggested) { typed ->
            if (typed.isNullOrBlank()) {
                WaypointMenu(repo).openLater(p, page); return@open
            }
            repo.saveById(
                UUID.randomUUID(), typed,
                loc.world!!.uid, loc.x, loc.y, loc.z,
                loc.yaw, loc.pitch, p.uniqueId
            )
            p.sendMessage(messages.text("saved", mapOf("name" to typed)))
            WaypointMenu(repo).openLater(p, page)
        }
    }

    /* --- 小ヘルパ --- */
    private fun WaypointMenu.openLater(p: Player, page: Int) =
        object : BukkitRunnable() { override fun run() { open(p, page) } }.runTask(plugin)
}
