package com.vadim.gamenet.models


/**
 * firstname
lastname
email
password
country
spoken_languages
gender
list_of_games
friend_list
 */

//TODO: continue with user creation + mongo/aws integration
class AppUser(
    val id: String = "",
    var userName: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var password: String = "",
    var country: String = "",
    val spokenLanguages: ArrayList<String> = arrayListOf(),
    var gender: String = "",
    val listOfGames: ArrayList<Game> = arrayListOf(),
    val friend_list: ArrayList<AppUser> = arrayListOf()
) {
    override fun toString(): String {
        return "AppUser(id=$id,userName = $userName firstName='$firstName', lastName='$lastName', email='$email', password='$password', country='$country', spokenLanguages=$spokenLanguages, gender='$gender', listOfGames=$listOfGames, friend_list=$friend_list)"
    }
}