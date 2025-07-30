// src/main/kotlin/com/example/waypointTp/WpTab.kt
package com.example.waypointTp

import com.example.waypointTp.repo.YamlWaypointRepository
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class WpTab(private val repo: YamlWaypointRepository) : TabCompleter {

    private val roots = listOf("ui", "set", "tp", "tpp", "del", "reload")

    override fun onTabComplete(sender: CommandSender, cmd: Command, alias: String, args: Array<out String>): MutableList<String> {
        return when (args.size) {
            1 -> roots.filter { it.startsWith(args[0], ignoreCase = true) }.toMutableList()
            2 -> when (args[0].lowercase()) {
                "tp", "del" -> repo.allNames().filter { it.startsWith(args[1], ignoreCase = true) }.toMutableList()
                "tpp"      -> Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[1], ignoreCase = true) }.toMutableList()
                else       -> mutableListOf()
            }
            else -> mutableListOf()
        }
    }
}
