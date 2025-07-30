// src/main/kotlin/com/example/waypointTp/i18n/Messages.kt
package com.example.waypointTp.i18n

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Messages(private val plugin: JavaPlugin) {
    private val mm = MiniMessage.miniMessage()
    private val file = File(plugin.dataFolder, "messages.yml")
    private val cfg = YamlConfiguration()

    fun load() {
        if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs()
        if (!file.exists()) {
            // resources/messages.yml を初回展開
            plugin.saveResource("messages.yml", false)
        }
        cfg.load(file)
    }

    fun text(key: String, placeholders: Map<String, String> = emptyMap()): Component {
        val raw = cfg.getString(key) ?: key
        var replaced = raw
        placeholders.forEach { (k, v) -> replaced = replaced.replace("<$k>", v) }
        // prefix を毎回先頭に付与
        val prefix = cfg.getString("prefix") ?: ""
        return mm.deserialize(prefix + replaced)
    }
}
