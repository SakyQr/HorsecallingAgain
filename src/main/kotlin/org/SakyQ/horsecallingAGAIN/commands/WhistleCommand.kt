package org.SakyQ.horsecallingAGAIN.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.SakyQ.horsecallingAGAIN.managers.HorseManager

class WhistleCommand(private val horseManager: HorseManager) : CommandExecutor {

    fun register(plugin: JavaPlugin) {
        plugin.getCommand("whistle")?.setExecutor(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can whistle for horses!")
            return true
        }

        horseManager.callHorse(sender)
        return true
    }
}