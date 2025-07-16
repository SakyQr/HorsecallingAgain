package org.SakyQ.horsecallingAGAIN

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.SakyQ.horsecallingAGAIN.managers.*
import org.SakyQ.horsecallingAGAIN.commands.*

class HorsecallingAGAIN : JavaPlugin(), Listener {

    lateinit var configManager: ConfigManager
    lateinit var dataManager: DataManager
    lateinit var horseManager: HorseManager
    lateinit var combatTracker: CombatTracker
    lateinit var movementTracker: MovementTracker

    override fun onEnable() {
        try {
            // Initialize configuration first
            configManager = ConfigManager(this)
            configManager.loadConfig()

            // Initialize data manager
            dataManager = DataManager(this, configManager)

            // Initialize other managers
            horseManager = HorseManager(this, configManager, dataManager)
            combatTracker = CombatTracker(this, configManager)
            movementTracker = MovementTracker(this, configManager)

            // Set cross-references
            combatTracker.setHorseManager(horseManager)
            movementTracker.setHorseManager(horseManager)

            // Load saved data
            horseManager.loadPlayerData()

            // Register events
            server.pluginManager.registerEvents(horseManager, this)
            server.pluginManager.registerEvents(combatTracker, this)
            server.pluginManager.registerEvents(movementTracker, this)
            server.pluginManager.registerEvents(this, this)

            // Register commands
            WhistleCommand(horseManager).register(this)
            TameCommand(horseManager).register(this)
            HorseInfoCommand(horseManager, dataManager).register(this)
            ReloadCommand(configManager).register(this)

            // Start auto-save
            dataManager.startAutoSave(horseManager.getPlayerHorseData())

            logger.info("Horse Calling plugin enabled with persistence!")

        } catch (e: Exception) {
            logger.severe("Failed to enable Horse Calling plugin: ${e.message}")
            e.printStackTrace()
            isEnabled = false
        }
    }

    override fun onDisable() {
        try {
            if (::horseManager.isInitialized && ::dataManager.isInitialized) {
                // FIXED: Use proper shutdown saving to avoid threading issues
                dataManager.saveDataOnShutdown(horseManager.getPlayerHorseData())
                horseManager.cleanup()
            }
            logger.info("Horse Calling plugin disabled!")

        } catch (e: Exception) {
            logger.severe("Error during plugin shutdown: ${e.message}")
            e.printStackTrace()
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        try {
            if (::configManager.isInitialized && configManager.shouldSaveOnLogout() && ::dataManager.isInitialized && ::horseManager.isInitialized) {
                // FIXED: Save on logout now uses thread-safe method
                dataManager.saveData(horseManager.getPlayerHorseData())
            }
        } catch (e: Exception) {
            logger.warning("Failed to save data on player quit for ${event.player.name}: ${e.message}")
        }
    }
}