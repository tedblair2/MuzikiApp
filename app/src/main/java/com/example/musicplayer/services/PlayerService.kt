package com.example.musicplayer.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.musicplayer.*
import com.example.musicplayer.R
import com.example.musicplayer.Resources.audioList
import com.example.musicplayer.model.Audio
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.NotificationUtil.IMPORTANCE_HIGH

const val notificationId=21
class PlayerService:Service(){
    companion object{
        var shuffle=false
        var repeat=false
        var isPlaying=false
        var actionnext="nextaction"
    }
    var arraylist= arrayListOf<Audio>()
    var position=-1
    private val binder:IBinder=ServiceBinder()
    var player:ExoPlayer?=null
    lateinit var playerControls: PlayerControls
    private lateinit var mediaSessionCompat:MediaSessionCompat
    lateinit var notificationManager: PlayerNotificationManager

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        if (player==null){
            player=ExoPlayer.Builder(applicationContext).build()
        }
        val audioAttributes=AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        player!!.setAudioAttributes(audioAttributes,true)
        mediaSessionCompat= MediaSessionCompat(baseContext,"Muziki")
    }
    inner class ServiceBinder:Binder(){
        fun getService():PlayerService=this@PlayerService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        position= intent?.getIntExtra("position",-1)!!
        val sender=intent.getStringExtra("sender")
        arraylist = if (sender != null && sender=="album"){
            getUniqueSongs(AlbumActivity.list)
        }else{
            getUniqueSongs(audioList)
        }
        player?.setMediaItems(getMediaItems(), position, 0)
        player?.prepare()
        player?.play()
        if (player!!.isPlaying) {
            player?.pause()
            player?.stop()
        }
        player!!.repeatMode=Player.REPEAT_MODE_ALL
        notificationManager=PlayerNotificationManager.Builder(applicationContext, notificationId,channel_id)
            .setNotificationListener(notificationListener)
            .setMediaDescriptionAdapter(descriptionAdapter)
            .setChannelImportance(IMPORTANCE_HIGH)
            .setSmallIconResourceId(R.drawable.p21)
            .setPlayActionIconResourceId(R.drawable.ic_play)
            .setPauseActionIconResourceId(R.drawable.ic_pause)
            .setPreviousActionIconResourceId(R.drawable.ic_previous)
            .setCustomActionReceiver(customActionReceiver)
            .setChannelDescriptionResourceId(R.string.app_name)
            .setChannelNameResourceId(R.string.app_name)
            .build()
        notificationManager.setPlayer(player)
        notificationManager.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notificationManager.setMediaSessionToken(mediaSessionCompat.sessionToken)
        notificationManager.setUseRewindAction(false)
        notificationManager.setUseFastForwardAction(false)
        notificationManager.setUseNextAction(false)

        when(intent.action){
            actionnext->{
                playerControls.skipNext()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        notificationManager.setPlayer(null)
        if(player != null){
            player?.stop()
            player?.release()
        }
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    private val customActionReceiver=object :PlayerNotificationManager.CustomActionReceiver{
        override fun createCustomActions(
            context: Context,
            instanceId: Int
        ): MutableMap<String, NotificationCompat.Action> {

            val nextIntent=Intent(applicationContext,PlayerReceiver::class.java).setAction(action_next)
            val nextPending=PendingIntent.getBroadcast(applicationContext,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT)

            val next=NotificationCompat.Action(R.drawable.ic_next,"next",nextPending)
            val map= hashMapOf<String,NotificationCompat.Action>()
            map["next"]=next
            return map
        }

        override fun getCustomActions(player: Player): MutableList<String> {
            val actionlist= mutableListOf<String>()
            actionlist.add("next")
            return actionlist
        }
        override fun onCustomAction(player: Player, action: String, intent: Intent) {
        }
    }
    private val notificationListener=object :PlayerNotificationManager.NotificationListener{
        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            super.onNotificationCancelled(notificationId, dismissedByUser)
            stopForeground(true)
            if (player!!.isPlaying) player!!.pause()
        }

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            super.onNotificationPosted(notificationId, notification, ongoing)
            startForeground(notificationId,notification)
        }

    }
    private val descriptionAdapter=object :PlayerNotificationManager.MediaDescriptionAdapter{
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return player.currentMediaItem!!.mediaMetadata.title!!
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val intent=Intent(applicationContext,PlayerActivty::class.java)
            return PendingIntent.getActivity(applicationContext,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return player.currentMediaItem!!.mediaMetadata.artist
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val art=player.currentMediaItem!!.mediaMetadata.artworkData
            val bitmap=if (art != null){
                BitmapFactory.decodeByteArray(art,0,art.size)
            }else{
                BitmapFactory.decodeResource(resources,R.drawable.p32)
            }
            return bitmap
        }
    }
    fun showNotification(play:Int){
        val intent=Intent(applicationContext,PlayerActivty::class.java)
        val pendingIntent=PendingIntent.getActivity(applicationContext,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)

        val prevIntent=Intent(applicationContext,PlayerReceiver::class.java).setAction(action_prev)
        val prevPending=PendingIntent.getBroadcast(applicationContext,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent=Intent(applicationContext,PlayerReceiver::class.java).setAction(action_play)
        val playPending=PendingIntent.getBroadcast(applicationContext,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent=Intent(applicationContext,PlayerReceiver::class.java).setAction(action_next)
        val nextPending=PendingIntent.getBroadcast(applicationContext,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val closeIntent=Intent(applicationContext,PlayerReceiver::class.java).setAction(action_close)
        val closePending=PendingIntent.getBroadcast(applicationContext,0,closeIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val art=player?.currentMediaItem!!.mediaMetadata.artworkData
        val bitmap=if(art != null){
            BitmapFactory.decodeByteArray(art,0,art.size)
        }else{
            BitmapFactory.decodeResource(resources,R.drawable.p32)
        }
        val notification=NotificationCompat.Builder(this, channel_id2)
            .setSmallIcon(R.drawable.p21)
            .setContentTitle(player?.currentMediaItem!!.mediaMetadata.title)
            .setContentText(player?.currentMediaItem!!.mediaMetadata.artist)
            .setLargeIcon(bitmap)
            .addAction(R.drawable.ic_previous,"Previous",prevPending)
            .addAction(play,"Play",playPending)
            .addAction(R.drawable.ic_next,"next",nextPending)
            .addAction(R.drawable.ic_close,"close",closePending)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionCompat.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        startForeground(notificationId,notification)
    }
    private fun getMediaItems(): MutableList<MediaItem> {
        val medialist= mutableListOf<MediaItem>()
        for (song in arraylist){
            val uri=Uri.parse(song.path)
            val item= MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(getMediaMetadata(song))
                .build()

            medialist.add(item)
        }
        return medialist
    }
    private fun getMediaMetadata(song: Audio): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(song.name)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(Uri.parse(song.path))
            .setArtworkData(getAlbumArt(song.path))
            .build()
    }
    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        return retriever.embeddedPicture
    }
    private fun getUniqueSongs(list:ArrayList<Audio>):ArrayList<Audio>{
        val set= list.toSet()
        return ArrayList(set)
    }

    fun playPause(){
        playerControls.playOrPause()
    }
    fun skipNext(){
        playerControls.skipNext()
    }
    fun setCallBack(playerControls: PlayerControls){
        this.playerControls=playerControls
    }
    fun skipPrevious(){
        playerControls.skipPrevious()
    }
}