package com.example.musicplayer.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentPlayerBinding
import com.example.musicplayer.services.PlayerControls
import com.example.musicplayer.services.PlayerService
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class PlayerFragment : Fragment(),ServiceConnection {
    private var _binding:FragmentPlayerBinding?=null
    private val binding get() = _binding!!
    private var player:ExoPlayer?=null
    private var service: PlayerService?=null
    private var isBound=false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding=FragmentPlayerBinding.inflate(inflater,container,false)

        if (PlayerService.isPlaying){
            binding.play.setImageResource(R.drawable.ic_pause)
        }else{
            binding.play.setImageResource(R.drawable.ic_play)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val intent=Intent(requireContext(),PlayerService::class.java)
        requireActivity().bindService(intent,this,Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound){
            requireActivity().unbindService(this)
            isBound=false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }


    private fun playerListiner(){
        player!!.addListener(object :Player.Listener{
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                binding.songName.text=mediaItem!!.mediaMetadata.title
                binding.songArtist.text=mediaItem.mediaMetadata.artist
                getAlbumImage(mediaItem.mediaMetadata.artworkData)
                binding.play.setImageResource(R.drawable.ic_pause)
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState==ExoPlayer.STATE_READY){
                    binding.songName.text=player?.currentMediaItem!!.mediaMetadata.title
                    binding.songArtist.text=player?.currentMediaItem!!.mediaMetadata.artist
                    binding.play.setImageResource(R.drawable.ic_pause)
                    getAlbumImage(player?.currentMediaItem!!.mediaMetadata.artworkData)
                }else{
                    binding.play.setImageResource(R.drawable.ic_play)
                }
            }
        })
    }
    private fun getAlbumImage(art:ByteArray?){
        if (art != null){
            val options = BitmapFactory.Options()
            options.inSampleSize = 2 // reduce sample size by half
            val bitmap = BitmapFactory.decodeByteArray(art, 0, art.size, options)
            binding.imgMusic.setImageBitmap(bitmap)
        }else{
            binding.imgMusic.setImageResource(R.drawable.p32)
        }
    }
    private fun checkPausePlay(){
        if(player != null && PlayerService.isPlaying){
            binding.play.setImageResource(R.drawable.ic_pause)
        }else{
            binding.play.setImageResource(R.drawable.ic_play)
        }
    }
    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        val binder=p1 as PlayerService.ServiceBinder
        service=binder.getService()
        player=binder.getService().player
        playerListiner()
        checkPausePlay()
        binding.next.setOnClickListener { service!!.skipNext() }
        binding.play.setOnClickListener {
            service!!.playPause()
            if (player!!.isPlaying){
                binding.play.setImageResource(R.drawable.ic_pause)
            }else{
                binding.play.setImageResource(R.drawable.ic_play)
            }
        }
        binding.prev.setOnClickListener { service!!.skipPrevious() }
    }
    override fun onServiceDisconnected(p0: ComponentName?) {
        isBound=false
    }
}