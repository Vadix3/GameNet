package com.vadim.gamenet.models

class FriendRequest(
    var id: String = "",
    var sender: AppUser = AppUser(),
    var time: Long = System.currentTimeMillis()
) {
    override fun toString(): String {
        return "FriendRequest(id='$id', sender=$sender, time=$time)"
    }
}