package com.vadim.gamenet.models

class AppUser(
    var user_id: String = "",
    var username: String = "",
    var first_name: String = "",
    var last_name: String = "",
    var email: String = "",
    var password: String = "",
    var country: String = "",
    var gender: String = "",
    var photo_url: String = "",
    var spoken_languages: ArrayList<String> = arrayListOf(),
    var games_list: ArrayList<Game> = arrayListOf(),
    var friends_list: ArrayList<AppUser> = arrayListOf(),
    var list_of_conversations: ArrayList<Conversation> = arrayListOf(),
    var friend_requests: ArrayList<FriendRequest> = arrayListOf()
) {
    override fun toString(): String {
        return "AppUser(user_id='$user_id', username='$username', first_name='$first_name', last_name='$last_name', email='$email', country='$country', gender='$gender', photo_url='$photo_url', spoken_languages=$spoken_languages, games_list=$games_list, friends_list=$friends_list, list_of_conversations=$list_of_conversations, friend_requests=$friend_requests)"
    }
}