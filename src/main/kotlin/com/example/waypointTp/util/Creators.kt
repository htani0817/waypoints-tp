// src/main/kotlin/com/example/waypointTp/util/Creators.kt
package com.example.waypointTp.util

import org.bukkit.Bukkit
import java.util.UUID

object Creators {
    /** UUID -> 表示用の作成者名（オンラインなら即時、オフラインは最後に見た名前。なければ短縮UUID） */
    fun displayNameOf(uuid: UUID): String {
        Bukkit.getPlayer(uuid)?.let { return it.name } // オンラインなら確実
        val off = Bukkit.getOfflinePlayer(uuid)         // オフライン名（null の場合あり）
        return off.name ?: uuid.toString().substring(0, 8)
    }
}

