package com.vadim.gamenet.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.models.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type
import kotlin.collections.ArrayList

class ParsingTools {


    companion object Functions {


        fun parseComment(temp: String): FeedComment {
            Log.d(TAG, "parseComment: $temp")
            val tempComment = FeedComment()
            val plainJson = JSONObject(temp)
            tempComment.id = plainJson.get("id") as String
            tempComment.comment = plainJson.get("comment") as String
            tempComment.commenter = plainJson.get("commenter") as String
            return tempComment
        }


        fun parsePost(temp: String): FeedPost {
            Log.d(TAG, "parsePost: $temp")
            val tempPost = FeedPost()
            val plainJson = JSONObject(temp)
            tempPost.id = plainJson.get("id") as String
            tempPost.user_id = plainJson.get("user_id") as String
            tempPost.username = plainJson.get("username") as String
            tempPost.profile_picture_uri = plainJson.get("profile_picture_uri") as String
            tempPost.like_count = plainJson.get("like_count") as Int
            tempPost.content = plainJson.get("content") as String
            tempPost.post_time =
                ((plainJson.get("post_time") as JSONObject).get("\$numberLong") as String).toLong()
            val commentsJsonArray = plainJson.get("comments") as JSONArray
            for (i in 0 until commentsJsonArray.length()) {
                val tempComment = parseComment(commentsJsonArray.get(i).toString())
                tempPost.comments.add(tempComment)
            }

            return tempPost
        }


        fun parseFriendRequest(temp: kotlin.String): FriendRequest {
            Log.d(MyAppClass.Constants.TAG, "parseFriendRequest: $temp")
            val tempRequest = FriendRequest()
            val plainJson = JSONObject(temp)
            tempRequest.id = plainJson.get(MongoTools.FRIEND_REQUEST_KEYS.ID) as kotlin.String
            tempRequest.receiver =
                plainJson.get(MongoTools.FRIEND_REQUEST_KEYS.RECEIVER) as kotlin.String
            tempRequest.sender =
                plainJson.get(MongoTools.FRIEND_REQUEST_KEYS.SENDER) as kotlin.String
            tempRequest.time =
                ((plainJson.get(MongoTools.FRIEND_REQUEST_KEYS.TIME) as JSONObject).get("\$numberLong") as kotlin.String).toLong()
            return tempRequest
        }

        fun parseUser(temp: kotlin.String): AppUser {
            Log.d(MyAppClass.Constants.TAG, "parseUser: $temp")
            val tempUser = AppUser()
            val plainJson = JSONObject(temp)
            tempUser.user_id = plainJson.get(MongoTools.USER_KEYS.USER_ID) as kotlin.String
            Log.d(TAG, "parseUser: id")
            tempUser.username = plainJson.get(MongoTools.USER_KEYS.USER_NAME) as kotlin.String
            Log.d(TAG, "parseUser: username")
            tempUser.first_name = plainJson.get(MongoTools.USER_KEYS.FIRST_NAME) as kotlin.String
            Log.d(TAG, "parseUser: fname")
            tempUser.last_name = plainJson.get(MongoTools.USER_KEYS.LAST_NAME) as kotlin.String
            Log.d(TAG, "parseUser: lname")
            tempUser.email = plainJson.get(MongoTools.USER_KEYS.EMAIL) as kotlin.String
            Log.d(TAG, "parseUser: email")
            tempUser.country = plainJson.get(MongoTools.USER_KEYS.COUNTRY) as kotlin.String
            Log.d(TAG, "parseUser: country")
            tempUser.gender = plainJson.get(MongoTools.USER_KEYS.GENDER) as kotlin.String
            Log.d(TAG, "parseUser: gender")
            tempUser.photo_url = plainJson.get(MongoTools.USER_KEYS.PHOTO_URL) as kotlin.String
            Log.d(TAG, "parseUser: photo")
            val stringType: Type = object : TypeToken<ArrayList<kotlin.String>>() {}.type
            tempUser.spoken_languages = Gson().fromJson(
                plainJson.get(MongoTools.USER_KEYS.SPOKEN_LANGUAGES) as kotlin.String,
                stringType
            ) as ArrayList<kotlin.String>
            Log.d(TAG, "parseUser: languages")
            val gameType: Type = object : TypeToken<ArrayList<Game>>() {}.type
            tempUser.games_list = Gson().fromJson(
                plainJson.get(MongoTools.USER_KEYS.GAMES_LIST) as kotlin.String, gameType
            ) as ArrayList<Game>
            Log.d(TAG, "parseUser: games")
            val userType: Type = object : TypeToken<ArrayList<AppUser>>() {}.type
            tempUser.friends_list = Gson().fromJson(
                plainJson.get(MongoTools.USER_KEYS.FRIENDS_LIST) as kotlin.String, stringType
            ) as ArrayList<kotlin.String>
            Log.d(TAG, "parseUser: friends")
            val conversationType: Type = object : TypeToken<ArrayList<String>>() {}.type
            tempUser.list_of_conversations = Gson().fromJson(
                plainJson.get(MongoTools.USER_KEYS.LIST_OF_CONVERSATIONS) as kotlin.String,
                conversationType
            ) as ArrayList<String>
            Log.d(TAG, "parseUser: conversations")
            return tempUser
        }


        fun parseConversation(temp: String): Conversation {
            Log.d(TAG, "parseConversation: $temp")
            val tempConversation = Conversation()
            val plainJson = JSONObject(temp)
            tempConversation.id = plainJson.get("id") as String
            tempConversation.imageLink = plainJson.get("imageLink") as String
            tempConversation.mode = plainJson.get("mode") as Int
            tempConversation.name = plainJson.get("name") as String
            val messageType: Type = object : TypeToken<Message>() {}.type
            val messagesArray = plainJson.get("messages") as JSONArray
            for (i in 0 until messagesArray.length()) {
                val item1 = messagesArray.get(i) as String
                val message = Gson().fromJson<Message>(item1, messageType)
                tempConversation.messages.add(message)
            }
            val participantType: Type = object : TypeToken<AppUser>() {}.type
            val usersArray = plainJson.get("participants") as JSONArray
            for (i in 0 until usersArray.length()) {
                val item = usersArray.get(i)
                val userItem = Gson().fromJson<AppUser>(item.toString(), participantType)
                tempConversation.participants.add(userItem)
            }

            return tempConversation
        }

    }
}