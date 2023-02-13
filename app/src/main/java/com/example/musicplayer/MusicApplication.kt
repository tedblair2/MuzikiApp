package com.example.musicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

const val channel_id="MusicPlayer"
const val channel_id2="Music"
const val action_prev="action_prev"
const val action_next="action_next"
const val action_play="action_play"
const val action_close="action_close"
class MusicApplication:Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channel=NotificationChannel(channel_id,"${R.string.app_name}",NotificationManager.IMPORTANCE_HIGH)
            channel.description="${R.string.app_name}"
            val channel2=NotificationChannel(channel_id2,"${R.string.app_name}",NotificationManager.IMPORTANCE_HIGH)
            channel2.description="${R.string.app_name}"
            val manager=getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            manager.createNotificationChannel(channel2)
        }
    }
}