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
                if (name.isNullOrBlank()) { sender.sendMessage(messages.text("usage_wp")); return true }
                val loc = sender.location
                val id = UUID.randomUUID()
                repo.saveById(
                    id = id, name = name,
                    world = loc.world!!.uid, x = loc.x, y = loc.y, z = loc.z,
                    yaw = loc.yaw, pitch = loc.pitch, creator = sender.uniqueId
                )
                sender.sendMessage(messages.text("saved", mapOf("name" to name)))
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
