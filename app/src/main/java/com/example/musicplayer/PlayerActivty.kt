package com.example.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.IBinder
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.example.musicplayer.databinding.PlayerLayoutBinding
import com.example.musicplayer.services.PlayerControls
import com.example.musicplayer.services.PlayerReceiver
import com.example.musicplayer.services.PlayerService
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PlayerActivty:AppCompatActivity(),ServiceConnection,PlayerControls{
    private lateinit var binding:PlayerLayoutBinding
    private var player: ExoPlayer?=null
    private var service:PlayerService?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=PlayerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val intent=Intent(this,PlayerService::class.java)
        bindService(intent,this, BIND_AUTO_CREATE)
        window.navigationBarColor=resources.getColor(R.color.colorPrimary)
        binding.songname.isSelected=true
        binding.back.setOnClickListener { onBackPressed() }
    }
    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
    }
    private fun seekBarList(){
        binding.seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, user: Boolean) {
                if (user){
                    player?.seekTo(p1.toLong())
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
    }
    private fun playerListiner(){
        player!!.addListener(object:Player.Listener{
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                binding.songname.text=mediaItem!!.mediaMetadata.title
                binding.songartist.text=mediaItem.mediaMetadata.artist
                binding.starttime.text=getTime(player!!.currentPosition)
                binding.seekbar.max= player!!.duration.toInt()
                binding.endtime.text=getTime(player!!.duration)
                binding.play.setImageResource(R.drawable.ic_pause)
//                if (!player!!.isPlaying) player!!.play()
                updatePlayerProgress()
                getArtWork(mediaItem.mediaMetadata.artworkData)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState==ExoPlayer.STATE_READY){
                    binding.songname.text=player!!.currentMediaItem!!.mediaMetadata.title
                    binding.songartist.text=player!!.currentMediaItem!!.mediaMetadata.artist
                    binding.starttime.text=getTime(player!!.currentPosition)
                    binding.seekbar.max= player!!.duration.toInt()
                    binding.endtime.text=getTime(player!!.duration)
                    binding.play.setImageResource(R.drawable.ic_pause)
//                    if (!player!!.isPlaying) player!!.play()
                    updatePlayerProgress()
                    getArtWork(player!!.currentMediaItem!!.mediaMetadata.artworkData)
                }else{
                    binding.play.setImageResource(R.drawable.ic_play)
                }
            }

        })
    }
    private fun loadAnimation(bitmap: Bitmap){
        val fadein= AnimationUtils.loadAnimation(this,android.R.anim.fade_in)
        val fadeout= AnimationUtils.loadAnimation(this,android.R.anim.fade_out)
        fadeout.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                binding.imageMusic.setImageBitmap(bitmap)
                fadein.setAnimationListener(object : Animation.AnimationListener{
                    override fun onAnimationStart(p0: Animation?) {
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                    }

                    override fun onAnimationRepeat(p0: Animation?) {
                    }

                })
                binding.imageMusic.startAnimation(fadein)
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }

        })
        binding.imageMusic.startAnimation(fadeout)
    }
    private fun getArtWork(art: ByteArray?) {
        var bitmap:Bitmap?=null
        if (art != null){
            bitmap= BitmapFactory.decodeByteArray(art,0,art.size)
            loadAnimation(bitmap)
            Palette.from(bitmap).generate { palette->
                val swatch=palette!!.dominantSwatch
                if (swatch != null){
                    binding.layout.setBackgroundResource(R.drawable.gradient_bg)
                    binding.gradient.setBackgroundResource(R.drawable.gradient_bg)
                    val gradientDrawable = GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        intArrayOf(swatch.rgb, 0x00000000)
                    )
                    binding.gradient.background=gradientDrawable
                    val gradientDrawable1 = GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        intArrayOf(swatch.rgb, swatch.rgb)
                    )
                    binding.layout.background=gradientDrawable1
                    binding.songname.setTextColor(swatch.titleTextColor)
                    binding.songartist.setTextColor(swatch.bodyTextColor)
                    binding.starttime.setTextColor(swatch.bodyTextColor)
                    binding.endtime.setTextColor(swatch.bodyTextColor)
                    binding.play.setColorFilter(swatch.bodyTextColor)
                    checkShuffle(swatch.bodyTextColor)
                    binding.prev.setColorFilter(swatch.bodyTextColor)
                    binding.next.setColorFilter(swatch.bodyTextColor)
                    binding.repeat.setColorFilter(swatch.bodyTextColor)
                    binding.seekbar.thumb.setTint(swatch.bodyTextColor)
                    binding.seekbar.progressDrawable.setTint(swatch.bodyTextColor)
                }else{
                    binding.layout.setBackgroundResource(R.drawable.gradient_bg)
                    binding.gradient.setBackgroundResource(R.drawable.gradient_bg)
                    val gradientDrawable = GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        intArrayOf(0xff000000.toInt(), 0x00000000)
                    )
                    binding.gradient.background=gradientDrawable
                    val gradientDrawable1 = GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        intArrayOf(0xff000000.toInt(), 0xff000000.toInt())
                    )
                    binding.layout.background=gradientDrawable1
                    binding.songname.setTextColor(Color.WHITE)
                    binding.songartist.setTextColor(Color.WHITE)
                    binding.starttime.setTextColor(Color.WHITE)
                    binding.endtime.setTextColor(Color.WHITE)
                    binding.play.setColorFilter(Color.WHITE)
                    checkShuffle(Color.WHITE)
                    binding.prev.setColorFilter(Color.WHITE)
                    binding.next.setColorFilter(Color.WHITE)
                    binding.repeat.setColorFilter(Color.WHITE)
                    binding.seekbar.thumb.setTint(Color.WHITE)
                    binding.seekbar.progressDrawable.setTint(Color.WHITE)
                }
            }
        }else{
            binding.imageMusic.setImageResource(R.drawable.p32)
            binding.gradient.setBackgroundResource(R.drawable.gradient_bg)
            binding.layout.setBackgroundResource(R.drawable.main_background)
            binding.songname.setTextColor(Color.WHITE)
            binding.songartist.setTextColor(Color.WHITE)
            binding.starttime.setTextColor(Color.WHITE)
            binding.endtime.setTextColor(Color.WHITE)
            binding.play.setColorFilter(Color.WHITE)
            checkShuffle(Color.WHITE)
            binding.prev.setColorFilter(Color.WHITE)
            binding.next.setColorFilter(Color.WHITE)
            binding.repeat.setColorFilter(Color.WHITE)
            binding.seekbar.thumb.setTint(Color.WHITE)
            binding.seekbar.progressDrawable.setTint(Color.WHITE)
        }
    }
    private fun repeat(){
        if (PlayerService.repeat){
            binding.repeat.setImageResource(R.drawable.ic_repeat)
            PlayerService.repeat=false
            player!!.repeatMode=ExoPlayer.REPEAT_MODE_ALL
        }else{
            binding.repeat.setImageResource(R.drawable.ic_repeat_one)
            PlayerService.repeat=true
            player!!.repeatMode=ExoPlayer.REPEAT_MODE_ONE
        }
    }
    private fun shuffle(){
        if (PlayerService.shuffle){
            PlayerService.shuffle=false
            binding.shuffle.setColorFilter(Color.LTGRAY)
            Toast.makeText(this, "Shuffle Off", Toast.LENGTH_SHORT).show()
            player!!.shuffleModeEnabled=false
        }else{
            PlayerService.shuffle=true
            binding.shuffle.setColorFilter(Color.BLUE)
            Toast.makeText(this, "Shuffle On", Toast.LENGTH_SHORT).show()
            player!!.shuffleModeEnabled=true
        }
    }
    private fun checkShuffle(color:Int){
        if (PlayerService.shuffle){
            binding.shuffle.setColorFilter(Color.BLUE)
        }else{
            binding.shuffle.setColorFilter(color)
        }
    }
    private fun checkRepeat(){
        if (PlayerService.repeat){
            binding.repeat.setImageResource(R.drawable.ic_repeat_one)
        }else{
            binding.repeat.setImageResource(R.drawable.ic_repeat)
        }
    }
    private fun getTime(duration: Long): String {
        val time = TimeUnit.MILLISECONDS.toSeconds(duration)
        val hours = time / 3600
        val minutes = time % 3600 / 60
        val seconds = time % 60
        return if (hours < 1) {
            if (seconds<10){
                "$minutes:0$seconds"
            }else{
                "$minutes:$seconds"
            }
        } else {
            if (minutes<10 && seconds<10){
                "$hours:0$minutes:0$seconds"
            }else if (minutes<10){
                "$hours:0$minutes:$seconds"
            }else if (seconds<10){
                "$hours:$minutes:0$seconds"
            }else{
                "$hours:$minutes:$seconds"
            }
        }
    }
    private fun updatePlayerProgress(){
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            if(player!!.isPlaying){
                binding.seekbar.progress=player!!.currentPosition.toInt()
                binding.starttime.text=getTime(player!!.currentPosition)
            }
            updatePlayerProgress()
        }
    }
    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        val binder=p1 as PlayerService.ServiceBinder
        service=binder.getService()
        player=binder.getService().player
        service!!.setCallBack(this)
        seekBarList()
        playerListiner()
        checkRepeat()
        if (player?.playbackState==ExoPlayer.STATE_READY){
            binding.songname.text=player?.currentMediaItem!!.mediaMetadata.title
            binding.songartist.text=player?.currentMediaItem!!.mediaMetadata.artist
            binding.starttime.text=getTime(player?.currentPosition!!)
            binding.seekbar.max= player?.duration!!.toInt()
            binding.endtime.text=getTime(player?.duration!!)
            binding.play.setImageResource(R.drawable.ic_pause)
            if (!player!!.isPlaying) player!!.play()
            updatePlayerProgress()
            getArtWork(player?.currentMediaItem!!.mediaMetadata.artworkData)
        }else{
            binding.play.setImageResource(R.drawable.ic_play)
        }
        binding.play.setOnClickListener { playOrPause()}
        binding.next.setOnClickListener { skipNext()}
        binding.prev.setOnClickListener {  skipPrevious()}
        binding.repeat.setOnClickListener { repeat() }
        binding.shuffle.setOnClickListener { shuffle() }
    }
    override fun onServiceDisconnected(p0: ComponentName?) {
        service=null
    }

    override fun playOrPause() {
        if (player!!.isPlaying){
            player!!.pause()
            PlayerService.isPlaying=false
            binding.play.setImageResource(R.drawable.ic_play)
        }else{
            player!!.play()
            PlayerService.isPlaying=true
            binding.play.setImageResource(R.drawable.ic_pause)
        }
    }

    override fun skipNext(){
        if (player!!.hasNextMediaItem()){
            player!!.seekToNext()
        }else{
            player!!.repeatMode=Player.REPEAT_MODE_ALL
        }
    }
    override fun skipPrevious(){
        if (player!!.hasPreviousMediaItem()){
            player!!.seekToPrevious()
        }
    }
}