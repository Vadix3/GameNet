package com.vadim.gamenet.interfaces

import com.vadim.gamenet.fragments.FragmentConversation
import com.vadim.gamenet.models.AppUser

interface OpenChatCallback {
    fun getFragment(fragment: FragmentConversation, name: String)
}