package com.example.musicplayer.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.musicplayer.*

class PlayerReceiver:BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val action=p1?.action
        if (action != null){
            when(action){
                action_next->{
                    val intent=Intent(p0,PlayerService::class.java)
                    intent.action=PlayerService.actionnext
                    p0?.startService(intent)
                }
            }
        }
    }
}