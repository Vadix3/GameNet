package com.vadim.gamenet.interfaces

import com.vadim.gamenet.models.AppUser

interface SelectedFriendsCallback {
    fun getSelectedItems(selectedList: BooleanArray)
}