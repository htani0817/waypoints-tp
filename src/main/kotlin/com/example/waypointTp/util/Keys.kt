package com.example.waypointTp.util

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

object Keys {
    lateinit var WP_ID: NamespacedKey
    lateinit var WP_OPENER: NamespacedKey
    lateinit var WP_TMP: NamespacedKey


    fun init(plugin: JavaPlugin) {
        WP_ID = NamespacedKey(plugin, "wp_id")
        WP_OPENER = NamespacedKey(plugin, "wp_opener")
        WP_TMP = NamespacedKey(plugin, "wp_tmp")
    }
}
