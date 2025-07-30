// src/main/kotlin/com/example/waypointTp/WaypointTp.kt
package com.example.waypointTp

import com.example.waypointTp.i18n.Messages
import com.example.waypointTp.repo.YamlWaypointRepository
import com.example.waypointTp.ui.MenuListener
import com.example.waypointTp.util.Keys
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
        logger.info("WaypointTp enabled")
    }
}
