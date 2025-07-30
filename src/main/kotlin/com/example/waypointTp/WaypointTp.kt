// src/main/kotlin/com/example/waypointTp/WaypointTp.kt
package com.example.waypointTp

import com.example.waypointTp.i18n.Messages
import com.example.waypointTp.repo.YamlWaypointRepository
import com.example.waypointTp.ui.MenuListener
import com.example.waypointTp.util.Keys
// ★ 追加
import com.example.waypointTp.ui.OpenMenuItemListener
import com.example.waypointTp.ui.OpenerDistributor

import org.bukkit.plugin.java.JavaPlugin

class WaypointTp : JavaPlugin() {
    lateinit var repo: YamlWaypointRepository
    lateinit var messages: Messages

    override fun onEnable() {
        Keys.init(this)
        messages = Messages(this).apply { load() }
        repo = YamlWaypointRepository(this)

        getCommand("wp")?.apply {
            setExecutor(WpCommand(this@WaypointTp, repo, messages))
            tabCompleter = WpTab(repo)
        }

        server.pluginManager.registerEvents(MenuListener(this, repo, messages), this)

        // ★ 追加：専用アイテムの右クリックで GUI を開く
        server.pluginManager.registerEvents(OpenMenuItemListener(this, repo), this)

        // ★ 追加：全員の左端へ専用アイテムを常時用意（参加時＋起動直後に適用）
        val distributor = OpenerDistributor(this, repo)
        server.pluginManager.registerEvents(distributor, this)
        distributor.ensureForAllOnline()

        logger.info("WaypointTp enabled")
    }
}
