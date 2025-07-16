package org.SakyQ.horsecallingAGAIN.managers

import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import org.SakyQ.horsecallingAGAIN.utils.ActionBarUtils
import java.util.*

class MovementTracker(
    private val plugin: JavaPlugin,
    private val configManager: ConfigManager
) : Listener {

    private lateinit var horseManager: HorseManager

    fun setHorseManager(manager: HorseManager) {
        this.horseManager = manager
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        // Skip if movement restrictions are disabled
        if (!configManager.isMovementRestrictionEnabled()) return

        val player = event.player
        val from = event.from
        val to = event.to

        // Only check if player actually moved (not just head turn)
        if (from.blockX == to.blockX && from.blockY == to.blockY && from.blockZ == to.blockZ) {
            return
        }

        if (!::horseManager.isInitialized) return

        val playerData = horseManager.getPlayerData(player.uniqueId)

        // Only track if horse is incoming
        if (playerData?.isHorseIncoming != true || playerData.callLocation == null) {
            return
        }

        // Get config values
        val maxDistance = configManager.getMaxMovementDistance()
        val warningDistance = configManager.getWarningDistance()

        // Calculate total distance moved from call location
        val distanceFromCallSpot = player.location.distance(playerData.callLocation!!)
        playerData.distanceMoved = distanceFromCallSpot

        when {
            distanceFromCallSpot >= maxDistance -> {
                // Horse gets lost!
                horseLost(player, playerData)
            }
            distanceFromCallSpot >= warningDistance -> {
                // Warning - horse might get lost
                if (configManager.isActionBarEnabled()) {
                    ActionBarUtils.sendActionBar(player, "§c⚠ Stay still or your horse will get lost! §e(${distanceFromCallSpot.toInt()}/${maxDistance.toInt()}m)")
                }
            }
            else -> {
                // Show normal timer with distance indicator (if enabled)
                if (configManager.isActionBarEnabled()) {
                    val timeLeft = horseManager.getTimeLeft(player.uniqueId)
                    if (timeLeft > 0) {
                        var message = "§6Horse arriving in: §e${timeLeft}s"
                        if (configManager.shouldShowMovementDistance()) {
                            message += " §7(moved ${distanceFromCallSpot.toInt()}m)"
                        }
                        ActionBarUtils.sendActionBar(player, message)
                    }
                }
            }
        }
    }

    private fun horseLost(player: Player, playerData: org.SakyQ.horsecallingAGAIN.data.PlayerHorseData) {
        player.sendMessage("§c§lYour horse got confused and lost track of you!")
        player.sendMessage("§7You moved too far from where you whistled (${playerData.distanceMoved.toInt()} blocks)")

        // Play lost sound if enabled
        if (configManager.isLostSoundEnabled()) {
            player.playSound(player.location, Sound.ENTITY_HORSE_DEATH, 0.5f, 1.5f)
        }

        // Cancel horse call
        horseManager.resetHorseCall(player.uniqueId)

        // Clear tracking data
        playerData.callLocation = null
        playerData.distanceMoved = 0.0
    }
}