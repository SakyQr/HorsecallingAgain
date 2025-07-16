package org.SakyQ.horsecallingAGAIN.data

import org.bukkit.Location
import java.util.*

data class PlayerHorseData(
    val playerId: UUID,
    var claimedHorseId: UUID? = null,
    var isHorseIncoming: Boolean = false,
    var callStartTime: Long = 0,
    var estimatedArrivalTime: Int = 0,
    // Movement tracking data
    var callLocation: Location? = null,
    var distanceMoved: Double = 0.0
)