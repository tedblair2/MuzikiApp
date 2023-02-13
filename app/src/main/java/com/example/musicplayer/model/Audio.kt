package com.example.musicplayer.model

data class Audio(
    val id:Int,
    val name:String,
    val artist:String,
    val album:String,
    val path:String,
    val duration: Long
)