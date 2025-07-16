package org.SakyQ.horsecallingAGAIN.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.SakyQ.horsecallingAGAIN.managers.HorseManager

class TameCommand(private val horseManager: HorseManager) : CommandExecutor {

    fun register(plugin: JavaPlugin) {
        plugin.getCommand("tame")?.setExecutor(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can tame horses!")
            return true
        }

        sender.sendMessage("§6=== Horse Taming Guide ===")
        sender.sendMessage("§e1. §fFind a wild horse")
        sender.sendMessage("§e2. §fRight-click with empty hand to tame")
        sender.sendMessage("§e3. §fShift + Right-click your tamed horse to claim it")
        sender.sendMessage("§e4. §fUse §a/whistle §fto call your claimed horse!")
        sender.sendMessage("§7Use §a/horseinfo §7to see all your horses")
        return true
    }
}