package com.vadim.gamenet.models

import java.util.*
import kotlin.String
import kotlin.collections.ArrayList

class FeedPost(
    var id: String = UUID.randomUUID().toString(),
    var user_id: String = "",
    var username: String = "",
    var profile_picture_uri: String = "",
    var like_count: Int = 0,
    var content: String = "",
    var comments: ArrayList<FeedComment> = arrayListOf(),
    var post_time: Long = System.currentTimeMillis()
) : Comparable<FeedPost> {


    override fun compareTo(other: FeedPost) = compareValuesBy(this, other,
        { it.post_time },
        { it.post_time }
    )

    override fun toString(): String {
        return "FeedPost(id='$id', userId='$user_id', userName='$username', profilePictureUri='$profile_picture_uri', likeCount=$like_count, message='$content', comments=$comments, postTime=$post_time)"
    }
}