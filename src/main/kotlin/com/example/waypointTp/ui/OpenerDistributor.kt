// src/main/kotlin/com/example/waypointTp/ui/OpenerDistributor.kt
package com.example.waypointTp.ui

import com.example.waypointTp.repo.YamlWaypointRepository
import com.example.waypointTp.util.Keys
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class OpenerDistributor(
    private val plugin: JavaPlugin,
    private val repo: YamlWaypointRepository
) : Listener {

    // ホットバーは 0..8。左端=0 を使います。
    private val TARGET_SLOT = 8

    /** プラグイン有効化時（再読み込み時等）に、既にオンラインの全員へ適用 */
    fun ensureForAllOnline() {
        for (player in Bukkit.getOnlinePlayers()) { // getOnlinePlayers は Collection を返します
            ensureOpenerAtTarget(player)
        }
    }

    /** 参加時にも必ず左端へ用意する */
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        ensureOpenerAtTarget(e.player)
    }

    private fun isOpenerItem(stack: ItemStack?): Boolean {
        val meta = stack?.itemMeta ?: return false
        return meta.persistentDataContainer.has(Keys.WP_OPENER, PersistentDataType.BYTE)
    }

    private fun ensureOpenerAtTarget(p: Player) {
        val inv = p.inventory
        val current = inv.getItem(TARGET_SLOT)

        // すでに左端が専用アイテムなら何もしない
        if (isOpenerItem(current)) return

        // どこかに既存の専用アイテムがあれば、左端と入れ替え
        val contents = inv.contents
        var existingIndex = -1
        for (i in contents.indices) {
            if (isOpenerItem(contents[i])) { existingIndex = i; break }
        }
        if (existingIndex >= 0) {
            val existing = inv.getItem(existingIndex)
            inv.setItem(existingIndex, current)
            inv.setItem(TARGET_SLOT, existing)
            p.updateInventory()
            return
        }

        // 専用アイテムが無い：左端に置く（左端に物があれば空きへ退避、満杯なら足元にドロップ）
        val opener = OpenMenuItemListener.createOpenerItem()
        if (current == null || current.type.isAir) {
            inv.setItem(TARGET_SLOT, opener)
        } else {
            val empty = inv.firstEmpty() // -1 なら満杯
            if (empty != -1) {
                inv.setItem(empty, current)
            } else {
                p.world.dropItemNaturally(p.location, current)
            }
            inv.setItem(TARGET_SLOT, opener)
        }
        p.updateInventory()
    }
}
