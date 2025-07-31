// src/main/kotlin/com/example/waypointTp/WaypointTp.kt
package com.example.waypointTp

import com.example.waypointTp.i18n.Messages
import com.example.waypointTp.repo.YamlWaypointRepository
import com.example.waypointTp.ui.MenuListener
import com.example.waypointTp.util.Keys
// ★ 追加
import com.example.waypointTp.ui.OpenMenuItemListener
import com.example.waypointTp.ui.OpenerDistributor
import com.example.waypointTp.ui.AnvilNamePrompt      // ★ 追加

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

        // ★ 金床プロンプトを用意してイベント登録
        val namePrompt = AnvilNamePrompt(this)
        server.pluginManager.registerEvents(namePrompt, this)

        // ★ MenuListener に namePrompt を渡す（シグネチャ更新）
        server.pluginManager.registerEvents(MenuListener(this, repo, messages, namePrompt), this)

        // 起動アイテム関連
        server.pluginManager.registerEvents(OpenMenuItemListener(this, repo), this)

        val distributor = OpenerDistributor(this, repo)
        server.pluginManager.registerEvents(distributor, this)
        distributor.ensureForAllOnline()

        logger.info("WaypointTp enabled")
    }
}
