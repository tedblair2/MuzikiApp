package com.example.musicplayer.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.MainActivity
import com.example.musicplayer.PassSong
import com.example.musicplayer.adapter.SongAdapter
import com.example.musicplayer.databinding.FragmentSongsBinding
import com.example.musicplayer.model.Audio

class SongsFragment : Fragment(){
    companion object{
        var songslist= arrayListOf<Audio>()
        var name=""
        var artist=""
    }
    private var _binding:FragmentSongsBinding?=null
    private val binding get() = _binding!!
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding= FragmentSongsBinding.inflate(inflater,container,false)
        val activity=activity as MainActivity
        songslist=getUniqueSongs(activity.getSongs())
        songAdapter= SongAdapter(songslist,requireContext())
        binding.recyclerSongs.setHasFixedSize(true)
        binding.recyclerSongs.layoutManager=LinearLayoutManager(requireContext())
        binding.recyclerSongs.adapter=songAdapter

        return binding.root
    }
    private fun getUniqueSongs(list:ArrayList<Audio>):ArrayList<Audio>{
        val set= list.toSet()
        return ArrayList(set)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

    fun searchSong(search: String) {
        val arraylist= arrayListOf<Audio>()
        if (songslist.size>0){
            for (song in songslist){
                if (song.name.lowercase().contains(search)){
                    arraylist.add(song)
                }
            }
        }
        songAdapter.searchSong(arraylist)
    }
}