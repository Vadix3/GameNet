package com.vadim.gamenet.models

import java.util.*
import kotlin.String

class Message(
    val id: String = UUID.randomUUID().toString(), // message id
    var sender: String = "", // sending user
    var messageText: String = "", // text
    val messageTime: Long = System.currentTimeMillis(), // message time
) {
    override fun toString(): String {
        return "Message(id=$id, sender='$sender', messageText='$messageText', messageTime=$messageTime)"
    }
}