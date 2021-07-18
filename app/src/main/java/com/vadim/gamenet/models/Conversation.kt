package com.vadim.gamenet.models

import java.util.*
import kotlin.String
import kotlin.collections.ArrayList



class Conversation(
    var id: String = UUID.randomUUID().toString(), // conversation id
    var mode: Int = 0, // 0=private 1=group
    var participants: ArrayList<AppUser> = arrayListOf(),
    var messages: ArrayList<Message> = arrayListOf(),
    var name: String = "",
    var imageLink: String = ""
) {


    object MODE {
        const val PRIVATE = 0
        const val GROUP = 1
    }

    override fun toString(): String {
        return "Conversation(id='$id', mode=$mode, participants=$participants, messages=$messages, name='$name', imageLink='$imageLink')"
    }
}