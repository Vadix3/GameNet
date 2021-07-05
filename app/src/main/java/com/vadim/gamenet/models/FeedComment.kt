package com.vadim.gamenet.models

class FeedComment(
    val commenter: AppUser = AppUser(),
    val comment: String = ""
) {
}