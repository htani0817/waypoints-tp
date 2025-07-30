// src/main/kotlin/com/example/waypointTp/WpCommand.kt
package com.example.waypointTp

import com.example.waypointTp.i18n.Messages
import com.example.waypointTp.repo.YamlWaypointRepository
import com.example.waypointTp.ui.WaypointMenu
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

class WpCommand(
    private val plugin: JavaPlugin,
    private val repo: YamlWaypointRepository,
    private val messages: Messages
) : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) { sender.sendMessage(messages.text("usage_wp")); return true }

        when (args[0].lowercase()) {
            "reload" -> {
                if (!sender.hasPermission("waypoints.reload")) { sender.sendMessage(messages.text("no_permission")); return true }
                messages.load()
                sender.sendMessage(messages.text("reloaded"))
                return true
            }
            "ui" -> {
                if (sender !is Player) { sender.sendMessage(messages.text("player_only")); return true }
                WaypointMenu(repo).open(sender, 0)
                return true
            }
            "set" -> {
                if (sender !is Player) { sender.sendMessage(messages.text("player_only")); return true }
                if (!sender.hasPermission("waypoints.create")) { sender.sendMessage(messages.text("no_permission")); return true }

                val name = args.getOrNull(1)
                if (name.isNullOrBlank()) { sender.sendMessage(messages.text("usage_wp_set_coords")); return true }

                // 既定値はプレイヤー現在値
                var world = sender.world
                var x = sender.location.x
                var y = sender.location.y
                var z = sender.location.z
                var yaw = sender.location.yaw
                var pitch = sender.location.pitch

                // 書式: /wp set <name> [x y z] [yaw pitch] [world]
                var i = 2
                fun dAt(idx: Int) = args.getOrNull(idx)?.toDoubleOrNull()
                fun fAt(idx: Int) = args.getOrNull(idx)?.toFloatOrNull()

                // x y z（3つそろっていれば採用、部分指定はエラー）
                val hasXYZ = args.size >= i + 3
                if (hasXYZ) {
                    val xx = dAt(i); val yy = dAt(i + 1); val zz = dAt(i + 2)
                    if (xx == null || yy == null || zz == null) {
                        sender.sendMessage(messages.text("invalid_number")); return true
                    }
                    x = xx; y = yy; z = zz; i += 3
                } else if (args.size > i) {
                    // 3つに満たない数値/文字が紛れていたらエラー
                    if (dAt(i) != null || dAt(i + 1) != null) {
                        sender.sendMessage(messages.text("invalid_number")); return true
                    }
                }

                // yaw pitch（2つそろっていれば採用、片方だけはエラー）
                val hasYawPitch = args.size >= i + 2
                if (hasYawPitch) {
                    val y0 = fAt(i); val p0 = fAt(i + 1)
                    if (y0 == null || p0 == null) {
                        sender.sendMessage(messages.text("invalid_number")); return true
                    }
                    yaw = y0; pitch = p0; i += 2
                } else if (args.size == i + 1 && fAt(i) != null) {
                    // 1つだけ数値が来たらエラー（もう1つが無い）
                    sender.sendMessage(messages.text("invalid_number")); return true
                }

                // world（余り1個があればワールド名とみなす）
                val worldName = args.getOrNull(i)
                if (!worldName.isNullOrBlank()) {
                    val w = Bukkit.getWorld(worldName)
                    if (w == null) {
                        sender.sendMessage(messages.text("world_not_found", mapOf("name" to worldName)))
                        return true
                    } else {
                        world = w // Bukkit.getWorld(name) で取得。:contentReference[oaicite:1]{index=1}
                    }
                }

                // Y をワールドの高さ範囲にクランプ（上端は maxHeight-1 が有効域）
                val minY = world.minHeight
                val maxY = world.maxHeight - 1
                if (y < minY) y = minY.toDouble()
                if (y > maxY) y = maxY.toDouble() // WorldInfo#getMin/MaxHeight。:contentReference[oaicite:2]{index=2}

                val id = UUID.randomUUID()
                repo.saveById(
                    id = id, name = name,
                    world = world.uid, x = x, y = y, z = z,
                    yaw = yaw, pitch = pitch, creator = sender.uniqueId
                )
                sender.sendMessage(
                    messages.text(
                        "saved_at",
                        mapOf(
                            "name" to name,
                            "x" to String.format("%.2f", x),
                            "y" to String.format("%.2f", y),
                            "z" to String.format("%.2f", z),
                            "world" to world.name
                        )
                    )
                )
                return true
            }
            "tp" -> {
                if (sender !is Player) { sender.sendMessage(messages.text("player_only")); return true }
                val name = args.getOrNull(1) ?: return sender.sendMessage(messages.text("usage_wp")).let { true }
                val wp = repo.findByName(name) ?: return sender.sendMessage(messages.text("not_found", mapOf("name" to name))).let { true }
                val loc = repo.toLocation(wp) ?: return sender.sendMessage(messages.text("not_found", mapOf("name" to name))).let { true }
                sender.sendMessage(messages.text("teleporting", mapOf("name" to wp.name)))
                sender.teleportAsync(loc).thenAccept { ok ->
                    if (!ok) sender.sendMessage(messages.text("teleport_failed"))
                }
                return true
            }
            "tpp" -> {
                if (sender !is Player) { sender.sendMessage(messages.text("player_only")); return true }
                val target = args.getOrNull(1)?.let { Bukkit.getPlayerExact(it) }
                if (target == null) { sender.sendMessage(messages.text("not_found", mapOf("name" to (args.getOrNull(1) ?: "")))); return true }
                sender.sendMessage(messages.text("teleporting", mapOf("name" to target.name)))
                sender.teleportAsync(target.location).thenAccept { ok ->
                    if (!ok) sender.sendMessage(messages.text("teleport_failed"))
                }
                return true
            }
            else -> { sender.sendMessage(messages.text("usage_wp")); return true }
        }
    }
}
