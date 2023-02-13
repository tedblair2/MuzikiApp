package com.example.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Resources.audioList
import com.example.musicplayer.adapter.DetailsAdapter
import com.example.musicplayer.databinding.ActivityAlbumBinding
import com.example.musicplayer.model.Audio
import com.example.musicplayer.services.PlayerService

class AlbumActivity : AppCompatActivity(),ServiceConnection {
    companion object{
        var list= arrayListOf<Audio>()
    }
    private lateinit var binding: ActivityAlbumBinding
    private var album:String?=null
    private lateinit var albumList:ArrayList<Audio>
    private lateinit var detailsAdapter: DetailsAdapter
    private var service:PlayerService?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        window.navigationBarColor=resources.getColor(R.color.colorPrimary)
        albumList= ArrayList()
        album=intent.getStringExtra("albumname")
        var j=0
        for (song in audioList){
            if (song.album == album){
                albumList.add(j,song)
                j++
            }
        }
        list=getUniqueSongs(albumList)
        val path=list[0].path
        val art=getAlbumArt(path)
        if (art != null){
            val bitmap=BitmapFactory.decodeByteArray(art,0,art.size)
            binding.albumImg.setImageBitmap(bitmap)
        }else{
            binding.albumImg.setImageResource(R.drawable.p32)
        }
        detailsAdapter= DetailsAdapter(list,this)
        binding.recyclerAlbumSongs.setHasFixedSize(true)
        binding.recyclerAlbumSongs.layoutManager=LinearLayoutManager(this)
        binding.recyclerAlbumSongs.adapter=detailsAdapter
    }
    private fun getUniqueSongs(list:ArrayList<Audio>):ArrayList<Audio>{
        val set= list.toSet()
        return ArrayList(set)
    }
    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        return retriever.embeddedPicture
    }

    override fun onResume() {
        super.onResume()
        val intent=Intent(this,PlayerService::class.java)
        bindService(intent,this, BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        if (service != null){
            unbindService(this)
        }
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        val binder=p1 as PlayerService.ServiceBinder
        service=binder.getService()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        service=null
    }
}