package com.example.musicplayer.adapter

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.AlbumActivity
import com.example.musicplayer.R
import com.example.musicplayer.databinding.AlbumLayoutBinding
import com.example.musicplayer.model.Audio

class AlbumAdapter(private var audioList:ArrayList<Audio>,private val context: Context):RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    class ViewHolder(val binding: AlbumLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=AlbumLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song=audioList[position]
        holder.binding.albumName.text=song.album
        val art=getAlbumArt(song.path)
        if (art != null){
            val bitmap=BitmapFactory.decodeByteArray(art,0,art.size)
            holder.binding.albumImg.setImageBitmap(bitmap)
        }else{
            holder.binding.albumImg.setImageResource(R.drawable.baseline_music_note_24)
        }

        holder.itemView.setOnClickListener {
            context.startActivity(Intent(context,AlbumActivity::class.java).apply {
                putExtra("albumname",song.album)
            })
        }
    }
    fun searchAlbum(filteredlist:ArrayList<Audio>){
        audioList=filteredlist
        notifyDataSetChanged()
    }
    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        return retriever.embeddedPicture
    }

}