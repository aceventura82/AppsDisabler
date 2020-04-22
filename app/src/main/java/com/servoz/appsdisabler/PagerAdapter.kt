package com.servoz.appsdisabler

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

// 1
class PagerAdapter(fragmentManager: FragmentManager, private val tags: ArrayList<TagView>) :
    FragmentStatePagerAdapter(fragmentManager) {

  // 2
  override fun getItem(position: Int): Fragment {
    return LauncherSlideFragment.newInstance(tags[position])
  }

  // 3
  override fun getCount(): Int {
    return tags.size
  }

  override fun getPageTitle(position: Int): CharSequence {
    return tags[position % tags.size].name
  }
}
