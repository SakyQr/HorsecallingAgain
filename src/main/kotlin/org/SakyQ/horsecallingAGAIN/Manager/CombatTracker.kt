package org.SakyQ.horsecallingAGAIN.managers

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class CombatTracker(
    private val plugin: JavaPlugin,
    private val configManager: ConfigManager
) : Listener {

    companion object {
        private val playersInCombat = mutableSetOf<UUID>()
        private val combatTasks = mutableMapOf<UUID, BukkitRunnable>()

        fun isInCombat(playerId: UUID): Boolean = playersInCombat.contains(playerId)
    }

    private lateinit var horseManager: HorseManager

    fun setHorseManager(manager: HorseManager) {
        this.horseManager = manager
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val attacker = event.damager as? Player
        val victim = event.entity as? Player

        attacker?.let { enterCombat(it) }
        victim?.let { enterCombat(it) }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val playerId = event.player.uniqueId
        playersInCombat.remove(playerId)
        combatTasks[playerId]?.cancel()
        combatTasks.remove(playerId)
    }

    private fun enterCombat(player: Player) {
        val playerId = player.uniqueId

        // Cancel existing combat task
        combatTasks[playerId]?.cancel()

        // Add to combat set
        playersInCombat.add(playerId)

        // Reset horse call if configured and manager is available
        if (configManager.shouldCombatCancelHorseCalls() && ::horseManager.isInitialized) {
            horseManager.resetHorseCall(playerId)
        }

        // Start combat timer using config duration
        val combatDuration = configManager.getCombatDuration()
        val task = object : BukkitRunnable() {
            override fun run() {
                playersInCombat.remove(playerId)
                combatTasks.remove(playerId)
            }
        }

        combatTasks[playerId] = task
        task.runTaskLater(plugin, (combatDuration * 20).toLong())
    }
}