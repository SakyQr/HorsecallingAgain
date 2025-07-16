package org.SakyQ.horsecallingAGAIN.utils

import org.bukkit.Sound
import org.bukkit.entity.Player

object SoundManager {

    fun playWhistleSound(player: Player) {
        player.playSound(player.location, Sound.ENTITY_PLAYER_BREATH, 1.0f, 0.8f)
        player.world.playSound(player.location, Sound.ENTITY_HORSE_BREATHE, 1.0f, 1.2f)
    }

    fun playHorseArrivalSound(player: Player) {
        player.playSound(player.location, Sound.ENTITY_HORSE_AMBIENT, 1.0f, 1.0f)
    }
}