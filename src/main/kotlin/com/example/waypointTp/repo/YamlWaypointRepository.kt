// src/main/kotlin/com/example/waypointTp/repo/YamlWaypointRepository.kt
package com.example.waypointTp.repo

import com.example.waypointTp.model.Waypoint
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class YamlWaypointRepository(private val plugin: JavaPlugin) {
    private val file = File(plugin.dataFolder, "waypoints.yml")
    private val cfg = YamlConfiguration()
    private val cache = ConcurrentHashMap<UUID, Waypoint>() // メモリ上の一覧はここから描画

    init {
        if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs()
        if (!file.exists()) file.createNewFile()
        cfg.load(file)
        loadAll()
    }

    private fun loadAll() {
        cache.clear()
        val sec = cfg.getConfigurationSection("waypoints") ?: return
        for (key in sec.getKeys(false)) {
            val id = runCatching { UUID.fromString(key) }.getOrNull() ?: continue
            val base = "waypoints.$key"
            val name = cfg.getString("$base.name") ?: continue
            val worldId = cfg.getString("$base.world")?.let(UUID::fromString) ?: continue
            val x = cfg.getDouble("$base.x")
            val y = cfg.getDouble("$base.y")
            val z = cfg.getDouble("$base.z")
            val yaw = cfg.getDouble("$base.yaw").toFloat()
            val pitch = cfg.getDouble("$base.pitch").toFloat()
            val creator = cfg.getString("$base.creator")?.let(UUID::fromString) ?: continue
            cache[id] = Waypoint(id, name, worldId, x, y, z, yaw, pitch, creator)
        }
    }

    fun all(): List<Waypoint> = cache.values.sortedBy { it.name.lowercase() }
    fun allNames(): List<String> = cache.values.map { it.name }.sorted()
    fun find(id: UUID): Waypoint? = cache[id]
    fun findByName(name: String): Waypoint? =
        cache.values.firstOrNull { it.name.equals(name, ignoreCase = true) }

    fun saveById(
        id: UUID,
        name: String,
        world: UUID,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        creator: UUID
    ) {
        val wp = Waypoint(id, name, world, x, y, z, yaw, pitch, creator)
        cache[id] = wp // ★ キャッシュ更新（UIが直後に反映される）
        val base = "waypoints.$id"
        cfg.set("$base.name", name)
        cfg.set("$base.world", world.toString())
        cfg.set("$base.x", x); cfg.set("$base.y", y); cfg.set("$base.z", z)
        cfg.set("$base.yaw", yaw); cfg.set("$base.pitch", pitch)
        cfg.set("$base.creator", creator.toString())
        cfg.save(file) // FileConfigurationの保存はsave呼び出しが必要。:contentReference[oaicite:2]{index=2}
    }

    fun delete(id: UUID) {
        cache.remove(id)
        cfg.set("waypoints.$id", null)
        cfg.save(file)
    }

    /** Waypoint -> Bukkit Location 変換 */
    fun toLocation(wp: Waypoint): org.bukkit.Location? {
        // Bukkit.getWorld(UUID) はUUIDでワールドを取得できるAPIです。:contentReference[oaicite:3]{index=3}
        val w: World = Bukkit.getWorld(wp.world) ?: return null
        return org.bukkit.Location(w, wp.x, wp.y, wp.z, wp.yaw, wp.pitch)
    }
}
