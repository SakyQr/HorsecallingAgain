package org.SakyQ.horsecallingAGAIN.utils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Horse
import org.bukkit.entity.Player
import kotlin.math.cos
import kotlin.math.sin

object LocationUtils {

    fun isPlayerUnderground(player: Player, depthThreshold: Int = 2): Boolean {
        val surfaceY = player.world.getHighestBlockYAt(player.location)
        return player.location.blockY < surfaceY - depthThreshold
    }

    fun findSafeLocationAroundPlayer(playerLocation: Location): Location {
        val world = playerLocation.world

        if (!isSafeWorld(playerLocation)) {
            return playerLocation.clone().add(0.0, 1.0, 0.0)
        }

        // Try locations in a circle around the player
        for (radius in 3..8) {
            for (angle in 0..360 step 30) {
                val radians = Math.toRadians(angle.toDouble())
                val x = playerLocation.x + radius * cos(radians)
                val z = playerLocation.z + radius * sin(radians)
                val y = world.getHighestBlockYAt(x.toInt(), z.toInt()).toDouble()

                val location = Location(world, x, y + 1, z)

                if (isSafeForHorse(location) && isChunkLoaded(location)) {
                    return location
                }
            }
        }

        // Fallback to player location
        return playerLocation.clone().add(0.0, 1.0, 0.0)
    }

    private fun isSafeForHorse(location: Location): Boolean {
        val block = location.block
        val above = location.clone().add(0.0, 1.0, 0.0).block
        val below = location.clone().subtract(0.0, 1.0, 0.0).block

        return below.type.isSolid &&
                block.type == Material.AIR &&
                above.type == Material.AIR
    }

    private fun isChunkLoaded(location: Location): Boolean {
        return location.world.isChunkLoaded(location.blockX shr 4, location.blockZ shr 4)
    }

    private fun isSafeWorld(location: Location): Boolean {
        val world = location.world
        return world.environment == World.Environment.NORMAL ||
                world.environment == World.Environment.THE_END
    }

    fun makeHorseLookAtPlayer(horse: Horse, player: Player) {
        val direction = player.location.subtract(horse.location).toVector().normalize()
        val newLocation = horse.location.clone()
        newLocation.direction = direction
        horse.teleport(newLocation)
    }
}