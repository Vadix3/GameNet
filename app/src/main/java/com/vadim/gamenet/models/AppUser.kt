package com.vadim.gamenet.models

class AppUser(
    var user_id: kotlin.String = "",
    var username: kotlin.String = "",
    var first_name: kotlin.String = "",
    var last_name: kotlin.String = "",
    var email: kotlin.String = "",
    var password: kotlin.String = "",
    var country: kotlin.String = "",
    var gender: kotlin.String = "",
    var photo_url: kotlin.String = "",
    var spoken_languages: ArrayList<kotlin.String> = arrayListOf(),
    var games_list: ArrayList<Game> = arrayListOf(),
    var friends_list: ArrayList<kotlin.String> = arrayListOf(),
    var list_of_conversations: ArrayList<String> = arrayListOf(),
) {
    override fun toString(): kotlin.String {
        return "AppUser(user_id='$user_id', username='$username', first_name='$first_name', last_name='$last_name', email='$email', country='$country', gender='$gender', photo_url='$photo_url', spoken_languages=$spoken_languages, games_list=$games_list, friends_list=$friends_list, list_of_conversations=$list_of_conversations)"
    }
}