package org.SakyQ.horsecallingAGAIN.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Horse
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.SakyQ.horsecallingAGAIN.managers.HorseManager
import org.SakyQ.horsecallingAGAIN.managers.DataManager
import java.text.SimpleDateFormat
import java.util.*

class HorseInfoCommand(
    private val horseManager: HorseManager,
    private val dataManager: DataManager
) : CommandExecutor {

    fun register(plugin: JavaPlugin) {
        plugin.getCommand("horseinfo")?.setExecutor(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can check horse info!")
            return true
        }

        showHorseInfo(sender)
        return true
    }

    private fun showHorseInfo(player: Player) {
        val playerData = horseManager.getPlayerData(player.uniqueId)
        val stats = dataManager.getPlayerStats(player.uniqueId)

        player.sendMessage("§6=== Your Horse Information ===")

        // Show claimed horse with detailed info
        if (playerData?.claimedHorseId != null) {
            val claimedHorse = Bukkit.getEntity(playerData.claimedHorseId!!) as? Horse
            if (claimedHorse != null && claimedHorse.isValid) {
                val horseName = claimedHorse.customName ?: "Unnamed Horse"
                val distance = player.location.distance(claimedHorse.location).toInt()

                player.sendMessage("§aActive Horse: §f$horseName §7(${distance} blocks away)")

                if (playerData.isHorseIncoming) {
                    player.sendMessage("§eStatus: §fComing to you!")
                } else {
                    player.sendMessage("§eStatus: §fWaiting for whistle")
                }

                // Show horse stats
                player.sendMessage("§7Horse Details:")
                player.sendMessage("  §7• Speed: §f${String.format("%.1f", claimedHorse.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED)?.value?.times(43.17) ?: 0.0)} blocks/sec")
                player.sendMessage("  §7• Health: §f${claimedHorse.health.toInt()}/${claimedHorse.maxHealth.toInt()}")
                player.sendMessage("  §7• Jump: §f${String.format("%.1f", claimedHorse.jumpStrength * 5)} blocks")

                // Show taming date if available
                stats?.let { statsMap ->
                    val tamedDate = statsMap["tamedDate"] as? Long
                    if (tamedDate != null && tamedDate > 0) {
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy")
                        player.sendMessage("  §7• Tamed: §f${dateFormat.format(Date(tamedDate))}")
                    }
                }

            } else {
                player.sendMessage("§cActive Horse: §fNot found (may have died)")
                horseManager.clearClaimedHorse(player.uniqueId)
            }
        } else {
            player.sendMessage("§cNo active horse claimed!")
        }

        // Show all tamed horses
        val tamedHorses = Bukkit.getWorlds().flatMap { world ->
            world.entities.filterIsInstance<Horse>()
                .filter { it.isTamed && it.owner == player }
        }

        if (tamedHorses.isNotEmpty()) {
            player.sendMessage("§6All your tamed horses (${tamedHorses.size}):")
            tamedHorses.forEachIndexed { index, horse ->
                val horseName = horse.customName ?: "Unnamed Horse #${index + 1}"
                val distance = player.location.distance(horse.location).toInt()
                val isActive = horse.uniqueId == playerData?.claimedHorseId
                val status = if (isActive) "§a[ACTIVE]" else "§7[Inactive]"

                player.sendMessage("  $status §f$horseName §7(${distance} blocks away in ${horse.world.name})")
            }
            player.sendMessage("§7Shift + Right-click any horse to make it your active horse!")
        } else {
            player.sendMessage("§7You don't have any tamed horses.")
        }
    }
}
