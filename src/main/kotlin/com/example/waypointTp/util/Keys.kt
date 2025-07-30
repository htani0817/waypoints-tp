package com.example.waypointTp.util

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

object Keys {
    lateinit var WP_ID: NamespacedKey
    fun init(plugin: JavaPlugin) {
        WP_ID = NamespacedKey(plugin, "wp_id")
    }
}
