package org.SakyQ.horsecallingAGAIN.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.SakyQ.horsecallingAGAIN.managers.ConfigManager

class ReloadCommand(private val configManager: ConfigManager) : CommandExecutor {

    fun register(plugin: JavaPlugin) {
        plugin.getCommand("horsereload")?.setExecutor(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("horsecalling.admin.reload")) {
            sender.sendMessage("§cYou don't have permission to reload the configuration!")
            return true
        }

        try {
            configManager.reloadConfiguration()
            sender.sendMessage("§aHorse Calling configuration reloaded successfully!")
        } catch (e: Exception) {
            sender.sendMessage("§cFailed to reload configuration: ${e.message}")
        }

        return true
    }
}