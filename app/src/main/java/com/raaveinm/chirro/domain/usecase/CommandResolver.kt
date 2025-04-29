package com.raaveinm.chirro.domain.usecase

import androidx.media3.exoplayer.ExoPlayer
import com.raaveinm.chirro.domain.PlayerService

class CommandResolver {
    fun commandResolver (){}

    fun playPauseCommand() : Boolean {
        if (PlayerService().getPlayer()!=null){
            if (PlayerService().getPlayer()!!.isPlaying){
                PlayerService().getPlayer()!!.pause(); return false
            }else{
                PlayerService().getPlayer()!!.play(); return true
            }
        } else { return false }
    }
}