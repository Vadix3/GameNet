package com.vadim.gamenet.models

import kotlin.String

class FriendRequest(
    var id: String = "",
    var sender: String = "",
    var receiver: String = "",
    var time: Long = System.currentTimeMillis()
) {
    override fun toString(): String {
        return "FriendRequest(id='$id', sender='$sender', receiver='$receiver', time=$time)"
    }
}