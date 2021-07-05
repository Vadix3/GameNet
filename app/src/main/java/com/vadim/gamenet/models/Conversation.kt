package com.vadim.gamenet.models

class Conversation(
    val id: String = "", // conversation id
    val participants: ArrayList<AppUser> = arrayListOf(), // participating users
    val messages: ArrayList<Message> = arrayListOf(), // conversation messages
    val name: String = "", // conversation name
) {
    override fun toString(): String {
        return "Conversation(id='$id', participants=$participants, messages=$messages, name='$name')"
    }
}