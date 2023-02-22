package com.example.musicplayer

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuProvider
import androidx.viewpager.widget.ViewPager
import com.example.musicplayer.Resources.audioList
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.fragments.AlbumFragment
import com.example.musicplayer.fragments.SongsFragment
import com.example.musicplayer.model.Audio

const val REQUEST_STORAGE=120
class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private val permission= WRITE_EXTERNAL_STORAGE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.navigationBarColor=resources.getColor(R.color.colorPrimary)
        requestStorage()
        binding.viewpager.addOnPageChangeListener(object :ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                invalidateOptionsMenu()
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })
        addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.tool_menu,menu)
                val search=menu.findItem(R.id.search)
                val searchview=search.actionView as androidx.appcompat.widget.SearchView
                searchview.queryHint="Search..."
                val searchText = searchview.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
                searchText.setTextColor(Color.WHITE)
                val closeButton = searchview.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
                closeButton.setColorFilter(Color.WHITE)
                searchview.setOnQueryTextListener(object :androidx.appcompat.widget.SearchView.OnQueryTextListener{
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        val current=binding.viewpager.currentItem
                        when(val fragment=viewPagerAdapter.getItem(current)){
                            is SongsFragment->fragment.searchSong(newText!!.lowercase())
                            is AlbumFragment->fragment.searchAlbum(newText!!.lowercase())
                        }
                        return true
                    }

                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return true
            }

        })
        binding.layout.setOnClickListener {
            startActivity(Intent(this,PlayerActivty::class.java))
        }
    }

    private fun initView() {
        viewPagerAdapter=ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(SongsFragment(),"Songs")
        viewPagerAdapter.addFragment(AlbumFragment(),"Albums")
        binding.viewpager.adapter=viewPagerAdapter
        binding.tablayout.setupWithViewPager(binding.viewpager)
    }

    private fun requestStorage(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,permission)){
            AlertDialog.Builder(this)
                .setTitle("Request Permission")
                .setMessage("Allow access to storage")
                .setPositiveButton("Ok"){_,_->
                    ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_STORAGE)
                }
                .setNegativeButton("Cancel"){dialog,_->
                    Toast.makeText(this, "Access to storage denied.Cannot fetch songs", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }.show()
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_STORAGE->{
                if ((grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED)){
                    initView()
                }else{
                    requestStorage()
                }
            }
        }
    }

    fun getSongs():ArrayList<Audio> {
        val list= arrayListOf<Audio>()
        val mediaStoreuri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }else{
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val projection= arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
        )
        val sort="${MediaStore.Audio.Media.DISPLAY_NAME.lowercase()} ASC"
        val cursor=contentResolver?.query(mediaStoreuri,projection,null,null,sort)
        if (cursor != null){
            while (cursor.moveToNext()){
                var name=cursor.getString(1)
                val artist=cursor.getString(2)
                val album=cursor.getString(3)
                val duration=cursor.getLong(4)
                val path=cursor.getString(6)
                val size=cursor.getLong(5)
                val id=cursor.getInt(0)
                if (size>1048576 && !name.endsWith(".amr")){
                    name=name.replace(".mp3","").replace(".wav","")
                    val audio=Audio(id,name,artist,album,path,duration)
                    list.add(audio)
                    audioList.add(audio)
                }
            }
            cursor.close()
        }
        return list
    }
}