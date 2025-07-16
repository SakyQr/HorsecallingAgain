package org.SakyQ.horsecallingAGAIN.managers

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Horse
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTameEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.SakyQ.horsecallingAGAIN.data.PlayerHorseData
import org.SakyQ.horsecallingAGAIN.utils.LocationUtils
import org.SakyQ.horsecallingAGAIN.utils.ActionBarUtils
import org.SakyQ.horsecallingAGAIN.utils.SoundManager
import java.util.*
import kotlin.math.ceil

class HorseManager(
    private val plugin: JavaPlugin,
    private val configManager: ConfigManager,
    private val dataManager: DataManager
) : Listener {

    private val playerHorseData = mutableMapOf<UUID, PlayerHorseData>()
    private val horseCallTasks = mutableMapOf<UUID, BukkitRunnable>()
    private val pendingHorseSwitches = mutableMapOf<UUID, UUID>()

    fun loadPlayerData() {
        playerHorseData.putAll(dataManager.loadData())
        plugin.logger.info("Loaded horse data for ${playerHorseData.size} players")
    }

    fun savePlayerData() {
        dataManager.saveData(playerHorseData)
    }

    fun getPlayerHorseData(): Map<UUID, PlayerHorseData> = playerHorseData

    fun callHorse(player: Player): Boolean {
        val playerData = getOrCreatePlayerData(player.uniqueId)

        // Check if player has a claimed horse
        if (playerData.claimedHorseId == null) {
            player.sendMessage("§cYou don't have a horse to call! Right-click a tamed horse to claim it.")
            return false
        }

        // Validation checks
        if (!canCallHorse(player)) {
            return false
        }

        val horse = Bukkit.getEntity(playerData.claimedHorseId!!) as? Horse
        if (horse == null || !horse.isValid) {
            player.sendMessage("§cYour horse could not be found!")
            playerData.claimedHorseId = null
            return false
        }

        // Calculate distance using config
        val distance = player.location.distance(horse.location)
        val maxDistance = configManager.getMaxCallDistance()

        if (distance > maxDistance) {
            player.sendMessage("§cYour horse is too far away to hear your whistle! (${distance.toInt()} blocks away, max ${maxDistance.toInt()})")
            return false
        }

        // Start horse journey
        startHorseJourney(player, horse, distance)
        return true
    }

    private fun canCallHorse(player: Player): Boolean {
        val playerData = playerHorseData[player.uniqueId] ?: return false

        // Check if player is underground (using config)
        if (!configManager.isUndergroundCallingAllowed() && LocationUtils.isPlayerUnderground(player, configManager.getUndergroundDepthThreshold())) {
            player.sendMessage("§cYour horse can't reach you underground!")
            return false
        }

        // Check if player is in combat (using config)
        if (configManager.shouldCombatCancelHorseCalls() && CombatTracker.isInCombat(player.uniqueId)) {
            player.sendMessage("§cYou can't call your horse while in combat!")
            resetHorseCall(player.uniqueId)
            return false
        }

        // Check if horse is already being called
        if (playerData.isHorseIncoming) {
            player.sendMessage("§eYour horse is already on its way!")
            return false
        }

        return true
    }

    private fun startHorseJourney(player: Player, horse: Horse, distance: Double) {
        val playerData = getOrCreatePlayerData(player.uniqueId)

        // Calculate travel time using config horse speed
        val travelTime = ceil(distance / configManager.getHorseSpeed()).toInt()

        playerData.callStartTime = System.currentTimeMillis()
        playerData.isHorseIncoming = true
        playerData.estimatedArrivalTime = travelTime

        // Store call location for movement tracking
        playerData.callLocation = player.location.clone()
        playerData.distanceMoved = 0.0

        // Play whistle sound (if enabled in config)
        if (configManager.isWhistleSoundEnabled()) {
            SoundManager.playWhistleSound(player)
        }

        player.sendMessage("§aYou whistle for your horse...")
        if (configManager.isMovementRestrictionEnabled()) {
            player.sendMessage("§7§oStay relatively still or your horse might get lost!")
        }

        // Start timer
        startActionBarTimer(player, travelTime, horse)
    }

    private fun startActionBarTimer(player: Player, totalTime: Int, horse: Horse) {
        // Only show action bar if enabled in config
        if (!configManager.isActionBarEnabled()) return

        // Cancel existing task
        horseCallTasks[player.uniqueId]?.cancel()

        val task = object : BukkitRunnable() {
            var timeLeft = totalTime

            override fun run() {
                val playerData = playerHorseData[player.uniqueId]

                if (playerData == null || !playerData.isHorseIncoming) {
                    ActionBarUtils.clearActionBar(player)
                    horseCallTasks.remove(player.uniqueId)
                    cancel()
                    return
                }

                if (timeLeft > 0) {
                    var message = "§6Horse arriving in: §e${timeLeft}s"

                    // Add movement distance if enabled
                    if (configManager.shouldShowMovementDistance() && playerData.callLocation != null) {
                        val distanceMoved = player.location.distance(playerData.callLocation!!).toInt()
                        message += " §7(moved ${distanceMoved}m)"
                    }

                    ActionBarUtils.sendActionBar(player, message)
                    timeLeft--
                } else {
                    onHorseArrival(player, horse)
                    horseCallTasks.remove(player.uniqueId)
                    cancel()
                }
            }
        }

        horseCallTasks[player.uniqueId] = task
        task.runTaskTimer(plugin, 0L, 20L)
    }

    private fun onHorseArrival(player: Player, horse: Horse) {
        val playerData = playerHorseData[player.uniqueId] ?: return
        playerData.isHorseIncoming = false
        playerData.callLocation = null
        playerData.distanceMoved = 0.0

        if (configManager.isActionBarEnabled()) {
            ActionBarUtils.clearActionBar(player)
        }

        player.sendMessage("§aYour horse has arrived!")

        // Play arrival sound if enabled
        if (configManager.isArrivalSoundEnabled()) {
            SoundManager.playHorseArrivalSound(player)
        }

        val safeLocation = LocationUtils.findSafeLocationAroundPlayer(player.location)
        horse.teleport(safeLocation)
        LocationUtils.makeHorseLookAtPlayer(horse, player)
    }

    fun claimHorse(player: Player, horse: Horse): Boolean {
        if (!horse.isTamed || horse.owner != player) {
            player.sendMessage("§cYou can only claim horses you have tamed!")
            return false
        }

        val playerData = getOrCreatePlayerData(player.uniqueId)

        // Check if player already has a claimed horse
        if (playerData.claimedHorseId != null) {
            val currentHorse = Bukkit.getEntity(playerData.claimedHorseId!!) as? Horse
            if (currentHorse != null && currentHorse.isValid) {
                player.sendMessage("§eYou already have a claimed horse! Do you want to switch to this one?")
                player.sendMessage("§7Shift + Right-click again within 5 seconds to confirm switch.")

                scheduleHorseSwitch(player, horse)
                return false
            }
        }

        // Claim the horse
        playerData.claimedHorseId = horse.uniqueId
        player.sendMessage("§aYou have claimed this horse! You can now whistle to call it.")
        player.sendMessage("§7Horse Name: ${horse.customName ?: "Unnamed Horse"}")

        // Save data immediately
        dataManager.saveData(playerHorseData)
        return true
    }

    private fun scheduleHorseSwitch(player: Player, newHorse: Horse) {
        pendingHorseSwitches[player.uniqueId] = newHorse.uniqueId

        object : BukkitRunnable() {
            override fun run() {
                pendingHorseSwitches.remove(player.uniqueId)
            }
        }.runTaskLater(plugin, 100L)
    }

    private fun confirmHorseSwitch(player: Player, horse: Horse): Boolean {
        val pendingHorseId = pendingHorseSwitches[player.uniqueId]

        if (pendingHorseId == horse.uniqueId) {
            val playerData = getOrCreatePlayerData(player.uniqueId)
            val oldHorse = Bukkit.getEntity(playerData.claimedHorseId!!) as? Horse

            playerData.claimedHorseId = horse.uniqueId
            pendingHorseSwitches.remove(player.uniqueId)

            player.sendMessage("§aYou have switched to this horse!")
            player.sendMessage("§7Old horse: ${oldHorse?.customName ?: "Unnamed"} → New horse: ${horse.customName ?: "Unnamed"}")

            // Save data immediately
            dataManager.saveData(playerHorseData)
            return true
        }

        return false
    }

    fun resetHorseCall(playerId: UUID) {
        val playerData = playerHorseData[playerId]
        if (playerData?.isHorseIncoming == true) {
            playerData.isHorseIncoming = false
            playerData.callLocation = null
            playerData.distanceMoved = 0.0

            horseCallTasks[playerId]?.cancel()
            horseCallTasks.remove(playerId)

            Bukkit.getPlayer(playerId)?.let { player ->
                if (configManager.isActionBarEnabled()) {
                    ActionBarUtils.clearActionBar(player)
                }
                player.sendMessage("§cYour horse got spooked and stopped coming!")
            }
        }
    }

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.rightClicked is Horse) {
            val horse = event.rightClicked as Horse
            val player = event.player

            if (horse.isTamed && horse.owner == player) {
                if (player.isSneaking) {
                    if (pendingHorseSwitches.containsKey(player.uniqueId)) {
                        confirmHorseSwitch(player, horse)
                    } else {
                        claimHorse(player, horse)
                    }
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onEntityTame(event: EntityTameEvent) {
        if (event.entity is Horse && event.owner is Player) {
            val player = event.owner as Player
            player.sendMessage("§aHorse tamed! Shift + Right-click to claim it for whistling.")
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val playerId = event.player.uniqueId

        // Clean up player data
        pendingHorseSwitches.remove(playerId)

        // Cancel any active horse call
        horseCallTasks[playerId]?.cancel()
        horseCallTasks.remove(playerId)

        // Save individual player data if configured
        if (configManager.shouldSaveOnLogout()) {
            dataManager.saveData(playerHorseData)
        }
    }

    // Public methods for other classes
    fun getPlayerData(playerId: UUID): PlayerHorseData? = playerHorseData[playerId]

    fun clearClaimedHorse(playerId: UUID) {
        playerHorseData[playerId]?.claimedHorseId = null
    }

    fun getTimeLeft(playerId: UUID): Int {
        val playerData = playerHorseData[playerId] ?: return 0
        if (!playerData.isHorseIncoming) return 0

        val elapsed = ((System.currentTimeMillis() - playerData.callStartTime) / 1000).toInt()
        return (playerData.estimatedArrivalTime - elapsed).coerceAtLeast(0)
    }

    private fun getOrCreatePlayerData(playerId: UUID): PlayerHorseData {
        return playerHorseData.getOrPut(playerId) { PlayerHorseData(playerId) }
    }

    fun cleanup() {
        horseCallTasks.values.forEach { it.cancel() }
        horseCallTasks.clear()
        savePlayerData()
    }
}