package com.example.musicplayer.services

import android.widget.ImageView

interface PlayerControls {
    fun playOrPause()
    fun skipNext()
    fun skipPrevious()
}