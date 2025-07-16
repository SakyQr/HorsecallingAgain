package org.SakyQ.horsecallingAGAIN.managers

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class ConfigManager(private val plugin: JavaPlugin) {

    private lateinit var config: FileConfiguration

    fun loadConfig() {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()
        config = plugin.config

        plugin.logger.info("Configuration loaded successfully!")
    }

    // Horse calling settings
    fun getMaxCallDistance(): Double = config.getDouble("horse-calling.max-call-distance", 75.0)
    fun getHorseSpeed(): Double = config.getDouble("horse-calling.horse-speed", 8.0)

    // Movement settings
    fun isMovementRestrictionEnabled(): Boolean = config.getBoolean("horse-calling.movement.enabled", true)
    fun getMaxMovementDistance(): Double = config.getDouble("horse-calling.movement.max-movement-distance", 15.0)
    fun getWarningDistance(): Double = config.getDouble("horse-calling.movement.warning-distance", 10.0)

    // Combat settings
    fun getCombatDuration(): Int = config.getInt("horse-calling.combat.duration", 10)
    fun shouldCombatCancelHorseCalls(): Boolean = config.getBoolean("horse-calling.combat.cancel-horse-calls", true)

    // Underground settings
    fun isUndergroundCallingAllowed(): Boolean = config.getBoolean("horse-calling.underground.allow-underground-calling", false)
    fun getUndergroundDepthThreshold(): Int = config.getInt("horse-calling.underground.depth-threshold", 2)

    // Sound settings
    fun isWhistleSoundEnabled(): Boolean = config.getBoolean("horse-calling.sounds.whistle-enabled", true)
    fun isArrivalSoundEnabled(): Boolean = config.getBoolean("horse-calling.sounds.arrival-enabled", true)
    fun isLostSoundEnabled(): Boolean = config.getBoolean("horse-calling.sounds.lost-enabled", true)

    // Action bar settings
    fun isActionBarEnabled(): Boolean = config.getBoolean("horse-calling.action-bar.enabled", true)
    fun shouldShowMovementDistance(): Boolean = config.getBoolean("horse-calling.action-bar.show-movement-distance", true)

    // Data settings
    fun getAutoSaveInterval(): Long = config.getLong("data.auto-save-interval", 1200L)
    fun shouldSaveOnLogout(): Boolean = config.getBoolean("data.save-on-logout", true)
    fun getMaxHorsesPerPlayer(): Int = config.getInt("data.max-horses-per-player", 10)

    fun reloadConfiguration() {
        loadConfig()
        plugin.logger.info("Configuration reloaded!")
    }
}
