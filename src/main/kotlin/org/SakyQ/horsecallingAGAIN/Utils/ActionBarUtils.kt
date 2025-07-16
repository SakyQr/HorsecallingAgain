package org.SakyQ.horsecallingAGAIN.utils

import org.bukkit.entity.Player

object ActionBarUtils {

    fun sendActionBar(player: Player, message: String) {
        try {
            // Try Spigot method first
            player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent(message)
            )
        } catch (e: Exception) {
            // Fallback for pure Bukkit servers
            player.sendMessage("§7[Horse] $message")
        }
    }

    fun clearActionBar(player: Player) {
        sendActionBar(player, "")
    }
}