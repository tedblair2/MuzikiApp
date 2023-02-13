package com.example.musicplayer.adapter

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.PassSong
import com.example.musicplayer.PlayerActivty
import com.example.musicplayer.R
import com.example.musicplayer.databinding.SongLayoutBinding
import com.example.musicplayer.fragments.PlayerFragment
import com.example.musicplayer.model.Audio
import com.example.musicplayer.services.PlayerService
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.util.concurrent.TimeUnit

class SongAdapter(private var arrayList: ArrayList<Audio>,private val context: Context):RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    class ViewHolder(val binding: SongLayoutBinding):RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongAdapter.ViewHolder {
        val view=SongLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongAdapter.ViewHolder, position: Int) {
        val song=arrayList[position]
        holder.binding.songname.text=song.name
        holder.binding.songduration.text=getTime(song.duration)
        val art=getAlbumArt(song.path)
        if (art != null){
            val bitmap=BitmapFactory.decodeByteArray(art,0,art.size)
            holder.binding.musicImag.setImageBitmap(bitmap)
        }else{
            holder.binding.musicImag.setImageResource(R.drawable.p32)
        }
        holder.itemView.setOnClickListener {
            val intent=Intent(context,PlayerService::class.java)
            intent.putExtra("position",position)
            context.startService(intent)
            context.startActivity(Intent(context,PlayerActivty::class.java))
        }

        holder.binding.more.setOnClickListener {
            val popupMenu=PopupMenu(context,it)
            popupMenu.menuInflater.inflate(R.menu.del_menu,popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menu->
                when(menu.itemId){
                    R.id.delete->{
                        deleteFile(position,it)
                        true
                    }else->false
                }
            }
            popupMenu.show()
        }
    }

    private fun deleteFile(position: Int, view: View) {
        val uri=ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,arrayList[position].id.toLong())
        val file=File(arrayList[position].path)
        val isdeleted=file.delete()
        if (isdeleted){
            context.contentResolver.delete(uri,null,null)
            arrayList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position,arrayList.size)
            Snackbar.make(view,"File Deleted", Snackbar.LENGTH_LONG).show()
        }else{
            Snackbar.make(view,"File Cannot be Deleted", Snackbar.LENGTH_LONG).show()
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

    override fun getItemCount(): Int {
        return arrayList.size
    }
    fun searchSong(filteredList:ArrayList<Audio>){
        arrayList=filteredList
        notifyDataSetChanged()
    }
    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        return retriever.embeddedPicture
    }
}