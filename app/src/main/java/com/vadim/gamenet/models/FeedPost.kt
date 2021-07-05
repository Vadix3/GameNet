package com.vadim.gamenet.models

class FeedPost(
    var id: String = "",
    var userId: String = "",
    var userName: String = "",
    var profilePictureUri: String = "",
    var likeCount: Int = 0,
    var message: String = "",
    var comments: ArrayList<String> = arrayListOf(),
    var postTime: Long = System.currentTimeMillis()
) : Comparable<FeedPost> {

    override fun toString(): String {
        return "FeedPost(id='$id', userId='$userId', likeCount=$likeCount, message='$message', comments=$comments, postTime=$postTime)"
    }

    override fun compareTo(other: FeedPost) = compareValuesBy(this, other,
        { it.postTime },
        { it.postTime }
    )
}