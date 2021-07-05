package com.vadim.gamenet.models

class Message(
    val id: String = "", // message id
    val sender: String = "", // sending user
    val messageText: String = "", // text
    val messageTime: Long = System.currentTimeMillis(), // message time
) {
    override fun toString(): String {
        return "Message(id=$id, sender='$sender', messageText='$messageText', messageTime=$messageTime)"
    }
}