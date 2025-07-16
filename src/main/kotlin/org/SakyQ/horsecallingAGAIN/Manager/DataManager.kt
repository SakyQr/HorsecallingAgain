package org.SakyQ.horsecallingAGAIN.managers

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Horse
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.SakyQ.horsecallingAGAIN.data.PlayerHorseData
import java.io.File
import java.util.*

class DataManager(private val plugin: JavaPlugin, private val configManager: ConfigManager) {

    private val dataFile = File(plugin.dataFolder, "playerdata.yml")
    private lateinit var dataConfig: YamlConfiguration

    fun loadData(): MutableMap<UUID, PlayerHorseData> {
        val playerData = mutableMapOf<UUID, PlayerHorseData>()

        if (!dataFile.exists()) {
            plugin.logger.info("No existing player data found, starting fresh.")
            return playerData
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile)

        val playersSection = dataConfig.getConfigurationSection("players") ?: return playerData

        for (playerIdString in playersSection.getKeys(false)) {
            try {
                val playerId = UUID.fromString(playerIdString)
                val playerSection = playersSection.getConfigurationSection(playerIdString) ?: continue

                val horseData = PlayerHorseData(playerId)

                // Load claimed horse ID
                val claimedHorseString = playerSection.getString("claimed-horse")
                if (claimedHorseString != null) {
                    horseData.claimedHorseId = UUID.fromString(claimedHorseString)
                }

                playerData[playerId] = horseData

            } catch (e: Exception) {
                plugin.logger.warning("Failed to load data for player $playerIdString: ${e.message}")
            }
        }

        plugin.logger.info("Loaded horse data for ${playerData.size} players.")
        return playerData
    }

    fun saveData(playerHorseData: Map<UUID, PlayerHorseData>) {
        // FIXED: Run on main thread to avoid async entity access
        object : BukkitRunnable() {
            override fun run() {
                saveDataSync(playerHorseData)
            }
        }.runTask(plugin)
    }

    private fun saveDataSync(playerHorseData: Map<UUID, PlayerHorseData>) {
        try {
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }

            dataConfig = YamlConfiguration()

            for ((playerId, horseData) in playerHorseData) {
                val playerPath = "players.$playerId"

                // Only save if player has a claimed horse
                if (horseData.claimedHorseId != null) {
                    dataConfig.set("$playerPath.claimed-horse", horseData.claimedHorseId.toString())

                    // FIXED: Only access entities on main thread
                    try {
                        val horse = Bukkit.getEntity(horseData.claimedHorseId!!) as? Horse
                        if (horse != null && horse.isValid) {
                            dataConfig.set("$playerPath.horse-name", horse.customName ?: "Unnamed Horse")
                            dataConfig.set("$playerPath.last-location.world", horse.world.name)
                            dataConfig.set("$playerPath.last-location.x", horse.location.x)
                            dataConfig.set("$playerPath.last-location.y", horse.location.y)
                            dataConfig.set("$playerPath.last-location.z", horse.location.z)
                            dataConfig.set("$playerPath.tamed-date", System.currentTimeMillis())
                        } else {
                            // Horse no longer exists, save basic info only
                            dataConfig.set("$playerPath.horse-name", "Unknown Horse")
                            dataConfig.set("$playerPath.last-seen", System.currentTimeMillis())
                        }
                    } catch (e: Exception) {
                        plugin.logger.warning("Could not access horse ${horseData.claimedHorseId} for player $playerId: ${e.message}")
                        // Save basic info even if horse access fails
                        dataConfig.set("$playerPath.horse-name", "Unknown Horse")
                        dataConfig.set("$playerPath.last-seen", System.currentTimeMillis())
                    }
                }
            }

            // FIXED: Save file asynchronously to avoid blocking main thread
            object : BukkitRunnable() {
                override fun run() {
                    try {
                        dataConfig.save(dataFile)
                        plugin.logger.info("Horse data saved successfully.")
                    } catch (e: Exception) {
                        plugin.logger.severe("Failed to save horse data file: ${e.message}")
                    }
                }
            }.runTaskAsynchronously(plugin)

        } catch (e: Exception) {
            plugin.logger.severe("Failed to prepare horse data for saving: ${e.message}")
        }
    }

    fun startAutoSave(playerHorseData: Map<UUID, PlayerHorseData>) {
        val interval = configManager.getAutoSaveInterval()

        // FIXED: Auto-save now runs on main thread, then saves async
        object : BukkitRunnable() {
            override fun run() {
                saveData(playerHorseData)
            }
        }.runTaskTimer(plugin, interval, interval) // Changed from runTaskTimerAsynchronously

        plugin.logger.info("Auto-save started with interval of ${interval / 20} seconds.")
    }

    fun getPlayerStats(playerId: UUID): Map<String, Any>? {
        if (!::dataConfig.isInitialized) {
            // Load config if not already loaded
            if (dataFile.exists()) {
                dataConfig = YamlConfiguration.loadConfiguration(dataFile)
            } else {
                return null
            }
        }

        val playerSection = dataConfig.getConfigurationSection("players.$playerId") ?: return null

        return mapOf(
            "horseName" to (playerSection.getString("horse-name") ?: "Unknown"),
            "tamedDate" to playerSection.getLong("tamed-date", 0L),
            "lastWorld" to (playerSection.getString("last-location.world") ?: "Unknown"),
            "lastSeen" to playerSection.getLong("last-seen", 0L)
        )
    }

    // FIXED: Add safe data saving for shutdown
    fun saveDataOnShutdown(playerHorseData: Map<UUID, PlayerHorseData>) {
        try {
            plugin.logger.info("Saving horse data on shutdown...")
            saveDataSync(playerHorseData)

            // Wait a moment for async file save to complete
            Thread.sleep(500)

        } catch (e: Exception) {
            plugin.logger.severe("Failed to save data on shutdown: ${e.message}")
        }
    }
}