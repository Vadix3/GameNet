package com.vadim.gamenet.models

import java.util.*
import kotlin.String

class FeedComment(
    var id : String = UUID.randomUUID().toString(),
    var commenter: String = "",
    var comment: String = ""
) {
    override fun toString(): String {
        return "FeedComment(id='$id', commenter='$commenter', comment='$comment')"
    }
}