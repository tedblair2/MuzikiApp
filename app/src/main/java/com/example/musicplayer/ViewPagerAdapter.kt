package com.example.musicplayer

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(fragmentManager: FragmentManager):FragmentPagerAdapter(fragmentManager) {
    private val fragments= arrayListOf<Fragment>()
    private val titles= arrayListOf<String>()
    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }

    fun addFragment(fragment: Fragment, title:String){
        fragments.add(fragment)
        titles.add(title)
    }
}