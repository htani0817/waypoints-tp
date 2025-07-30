// src/main/kotlin/com/example/waypointTp/WpTab.kt
package com.example.waypointTp

import com.example.waypointTp.repo.YamlWaypointRepository
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class WpTab(private val repo: YamlWaypointRepository) : TabCompleter {

    private val roots = listOf("ui", "set", "tp", "tpp", "del", "reload")

    override fun onTabComplete(sender: CommandSender, cmd: Command, alias: String, args: Array<out String>): MutableList<String> {
        return when (args.size) {
            1 -> roots.filter { it.startsWith(args[0], ignoreCase = true) }.toMutableList()

            2 -> when (args[0].lowercase()) {
                "tp", "del" ->
                    repo.allNames().filter { it.startsWith(args[1], ignoreCase = true) }.toMutableList()
                "tpp" ->
                    Bukkit.getOnlinePlayers().map { it.name }
                        .filter { it.startsWith(args[1], ignoreCase = true) }.toMutableList()
                "set" -> mutableListOf("<name>")
                else -> mutableListOf()
            }

            // /wp set <name> [x] [y] [z] [yaw] [pitch] [world]
            3 -> if (args[0].equals("set", true) && sender is Player)
                mutableListOf(sender.location.x.toInt().toString()) else mutableListOf()
            4 -> if (args[0].equals("set", true) && sender is Player)
                mutableListOf(sender.location.y.toInt().toString()) else mutableListOf()
            5 -> if (args[0].equals("set", true) && sender is Player)
                mutableListOf(sender.location.z.toInt().toString()) else mutableListOf()
            6 -> if (args[0].equals("set", true) && sender is Player)
                mutableListOf(sender.location.yaw.toInt().toString()) else mutableListOf()
            7 -> if (args[0].equals("set", true) && sender is Player)
                mutableListOf(sender.location.pitch.toInt().toString()) else mutableListOf()
            8 -> if (args[0].equals("set", true))
                Bukkit.getWorlds().map { it.name }
                    .filter { it.startsWith(args[7], ignoreCase = true) }.toMutableList()
            else mutableListOf()

            else -> mutableListOf()
        }
    }
}
