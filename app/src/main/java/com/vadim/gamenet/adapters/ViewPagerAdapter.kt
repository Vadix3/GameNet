package com.vadim.gamenet.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vadim.gamenet.fragments.FragmentChat
import com.vadim.gamenet.fragments.FragmentMainFeed
import com.vadim.gamenet.fragments.FragmentProfile
import com.vadim.gamenet.fragments.FragmentFriends
import com.vadim.gamenet.models.AppUser


class ViewPagerAdapter(
    context: Context,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    myUser: AppUser
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    private val myUser = myUser
    private val myContext = context

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return FragmentMainFeed(myUser)
            }
            1 -> {
                return FragmentFriends(myUser)
            }
            2 -> {
                return FragmentChat(myUser,myContext)
            }
            3 -> {
                return FragmentProfile(myUser)
            }
        }
        return FragmentFriends(myUser)
    }

    override fun getItemCount(): Int {
        return 4
    }
}