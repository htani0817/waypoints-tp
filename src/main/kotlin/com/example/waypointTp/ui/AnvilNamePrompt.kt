package com.example.waypointTp.ui

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.view.AnvilView
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

/**
 * 金床の名前入力（リネーム）を提示し、結果をコールバックするユーティリティ。
 * Paper 1.21+ の MenuType.ANVIL と AnvilView#getRenameText() を利用。
 */
class AnvilNamePrompt(private val plugin: JavaPlugin) : Listener {

    private val pending = mutableMapOf<UUID, (String?) -> Unit>()

    /**
     * 金床UIを開いて名前を入力してもらう。
     * @param suggested 金床左スロットに置く紙の初期表示名
     * @param onComplete 確定: 文字列 / キャンセル: null
     */
    fun open(player: Player, suggested: String, onComplete: (String?) -> Unit) {
        pending[player.uniqueId] = onComplete

        // 同tickのGUI競合を避けるため、次tickでオープン
        object : BukkitRunnable() {
            override fun run() {
                // Paper 1.21: 新メニューAPIで金床のビューを作成して開く
                val view = MenuType.ANVIL.create(player, Component.text("ウェイポイント名を入力"))
                player.openInventory(view) // HumanEntity#openInventory(InventoryView)

                // renameText が機能するよう、左スロットにアイテムを置く（例: 紙）
                val anvil = view.topInventory as AnvilInventory
                val paper = ItemStack(Material.PAPER).apply {
                    itemMeta = itemMeta.apply { displayName(Component.text(suggested)) }
                }
                anvil.setItem(0, paper)
            }
        }.runTask(plugin)
    }

    /** 結果スロット（rawSlot=2）クリックで確定。renameText を取得して閉じる。 */
    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val view = e.view
        if (view !is AnvilView) return
        val p = e.whoClicked as? Player ?: return
        val cb = pending[p.uniqueId] ?: return

        if (e.rawSlot == 2) {
            e.isCancelled = true
            val text = view.renameText?.trim()?.takeIf { it.isNotEmpty() }
            pending.remove(p.uniqueId)
            p.closeInventory()
            object : BukkitRunnable() {
                override fun run() { cb(text) }
            }.runTask(plugin)
        }
    }

    /** 画面を閉じられたらキャンセル扱い */
    @EventHandler
    fun onClose(e: InventoryCloseEvent) {
        val view = e.view
        if (view !is AnvilView) return
        val p = e.player as? Player ?: return
        val cb = pending.remove(p.uniqueId) ?: return

        object : BukkitRunnable() {
            override fun run() { cb(null) }
        }.runTask(plugin)
    }
}
