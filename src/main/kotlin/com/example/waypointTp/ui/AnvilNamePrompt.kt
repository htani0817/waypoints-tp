// src/main/kotlin/com/example/waypointTp/ui/AnvilNamePrompt.kt
package com.example.waypointTp.ui

import com.example.waypointTp.util.Keys
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.view.AnvilView
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

class AnvilNamePrompt(private val plugin: JavaPlugin) : Listener {

    private val pending = mutableMapOf<UUID, (String?) -> Unit>()

    fun open(player: Player, suggested: String, onComplete: (String?) -> Unit) {
        pending[player.uniqueId] = onComplete
        object : BukkitRunnable() {
            override fun run() {
                val view = MenuType.ANVIL.create(player, Component.text("ウェイポイント名を入力"))
                player.openInventory(view)

                val anvil = view.topInventory as AnvilInventory
                val paper = ItemStack(Material.PAPER).apply {
                    itemMeta = itemMeta.apply {
                        displayName(Component.text(suggested))
                        // ★ この紙は「入力用のダミー」だと分かるようタグ付け
                        persistentDataContainer.set(Keys.WP_TMP, PersistentDataType.BYTE, 1.toByte())
                    }
                }
                anvil.setItem(0, paper)
            }
        }.runTask(plugin)
    }

    /** 結果スロットクリックで確定（アイテムは渡さない） */
    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val view = e.view
        if (view !is AnvilView) return
        val p = e.whoClicked as? Player ?: return
        val cb = pending[p.uniqueId] ?: return

        val top = view.topInventory as AnvilInventory

        // ★ 入力スロット(0,1)は全面禁止（持ち出し・差し込みを防止）
        if (e.clickedInventory === top) {
            // 0:左入力, 1:右素材, 2:結果
            if (e.rawSlot == 0 || e.rawSlot == 1) {
                e.isCancelled = true
                return
            }
        }

        // ★ シフトクリックや数字キー移動での“上側への移動”も禁止
        if (e.isShiftClick || e.click == ClickType.NUMBER_KEY) {
            e.isCancelled = true
            return
        }

        // ★ 結果スロット(2)で確定（renameText を拾って閉じる）
        if (e.rawSlot == 2) {
            e.isCancelled = true
            val text = view.renameText?.trim()?.takeIf { it.isNotEmpty() }
            pending.remove(p.uniqueId)
            // 念のためクリアしてから閉じる
            top.clear()
            p.closeInventory()
            object : BukkitRunnable() {
                override fun run() { cb(text) }
            }.runTask(plugin)
        }
    }

    /** ドラッグで上側(0,1,2)に入れられないようにする */
    @EventHandler
    fun onDrag(e: InventoryDragEvent) {
        val view = e.view
        if (view !is AnvilView) return
        // 上側の範囲（rawSlot 0..2）が含まれるならキャンセル
        if (e.rawSlots.any { it in 0..2 }) {
            e.isCancelled = true
        }
    }

    /** 閉じられたらキャンセル扱い＋上側を空に */
    @EventHandler
    fun onClose(e: InventoryCloseEvent) {
        val view = e.view
        if (view !is AnvilView) return
        val p = e.player as? Player ?: return
        val cb = pending.remove(p.uniqueId) ?: return

        val top = view.topInventory as AnvilInventory
        top.clear() // ★ 入力物を完全に削除（返却/ドロップ防止）

        object : BukkitRunnable() {
            override fun run() { cb(null) }
        }.runTask(plugin)
    }
}
