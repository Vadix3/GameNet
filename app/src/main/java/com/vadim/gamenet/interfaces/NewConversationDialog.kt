package com.vadim.gamenet.interfaces

import com.vadim.gamenet.models.Conversation

interface NewConversationDialog {
    fun getConversation(conversation: Conversation)
}