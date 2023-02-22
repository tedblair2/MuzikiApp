package com.example.musicplayer.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicplayer.MainActivity
import com.example.musicplayer.adapter.AlbumAdapter
import com.example.musicplayer.databinding.FragmentAlbumBinding
import com.example.musicplayer.model.Audio

class AlbumFragment : Fragment() {
    private var _binding:FragmentAlbumBinding?=null
    private val binding get() = _binding!!
    private lateinit var albumAdapter: AlbumAdapter
    private var albumlist= arrayListOf<Audio>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding= FragmentAlbumBinding.inflate(inflater,container,false)
        val activity=activity as MainActivity
        albumlist=getUniqueAlbums(activity.getSongs())
        albumAdapter= AlbumAdapter(albumlist,requireContext())
        binding.recyclerAlbum.layoutManager=GridLayoutManager(requireContext(),2)
        binding.recyclerAlbum.adapter=albumAdapter
        albumAdapter.notifyDataSetChanged()

        return binding.root
    }
    private fun getUniqueAlbums(list: ArrayList<Audio>): ArrayList<Audio> {
        val uniqueAlbums = mutableSetOf<String>()
        val result = arrayListOf<Audio>()
        for (audio in list) {
            if (uniqueAlbums.add(audio.album)) {
                result.add(audio)
            }
        }
        return result
    }
    fun searchAlbum(search:String){
        val list= arrayListOf<Audio>()
        if (albumlist.size>0){
            for (song in albumlist){
                if (song.album.lowercase().contains(search)){
                    list.add(song)
                }
            }
        }
        albumAdapter.searchAlbum(list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}