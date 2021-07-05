package com.vadim.gamenet.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.models.*
import io.realm.mongodb.App
import io.realm.mongodb.User
import io.realm.mongodb.mongo.MongoClient
import io.realm.mongodb.mongo.MongoCollection
import io.realm.mongodb.mongo.MongoDatabase
import io.realm.mongodb.mongo.iterable.MongoCursor
import org.bson.Document
import org.json.JSONObject
import java.lang.reflect.Type

class MongoTools(context: Context, user: User, resultListener: ResultListener) {

    object KEYS {
        const val USER_ID = "user_id"
        const val USER_NAME = "username"
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val EMAIL = "email"
        const val COUNTRY = "country"
        const val GENDER = "gender"
        const val SPOKEN_LANGUAGES = "spoken_languages"
        const val GAMES_LIST = "games_list"
        const val FRIENDS_LIST = "friends_list"
        const val PHOTO_URL = "photo_url"
        const val LIST_OF_CONVERSATIONS = "list_of_conversations"
        const val FRIEND_REQUESTS = "friend_requests"
    }


    private val resultListener = resultListener
    private val mongoUser = user
    private val mongoClient: MongoClient =
        mongoUser.getMongoClient("mongodb-atlas")!!

    interface ResultListener {
        fun getResult(result: Boolean, message: String)
        fun getQueriedUsers(result: Boolean, message: String, userList: ArrayList<AppUser>)
    }

    fun saveUserCustomData(key: String, value: String, userEmail: String) {
        Log.d(TAG, "saveUserCustomData: Key: $key Value: $value userEmail: $userEmail")
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase("gamenet_users")!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection("custom_data")!!
        val queryFilter = Document(KEYS.EMAIL, userEmail)
        val updateDocument = Document(key, value)
        val updateDocument2 = Document("\$set", updateDocument)
        mongoCollection.updateOne(queryFilter, updateDocument2).getAsync { task ->
            if (task.isSuccess) {
                val count = task.get().modifiedCount
                if (count == 1L) {
                    Log.v(TAG, "successfully updated a document.")
                    resultListener.getResult(true, "Successfully updated a document")
                } else {
                    Log.v(TAG, "did not update a document.")
                    resultListener.getResult(false, "did not update: ${task.error}")
                }
            } else {
                Log.e(TAG, "failed to update document with: ${task.error}")
            }
        }

    }


    fun saveUserToDatabase(user: User, tempUser: AppUser) {
        Log.d(TAG, "saveUserCustomData: ")
        val mongoClient: MongoClient =
            user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase("gamenet_users")!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection("custom_data")!!
        Log.d(TAG, "saveUserCustomData: userID = ${user.id}")
        mongoCollection.insertOne(
            Document(KEYS.USER_ID, user.id)
                .append(KEYS.USER_NAME, tempUser.username)
                .append(KEYS.FIRST_NAME, tempUser.first_name)
                .append(KEYS.LAST_NAME, tempUser.last_name)
                .append(KEYS.EMAIL, tempUser.email)
                .append(KEYS.COUNTRY, tempUser.country)
                .append(KEYS.GENDER, tempUser.gender)
                .append(KEYS.PHOTO_URL, tempUser.photo_url)
                .append(KEYS.SPOKEN_LANGUAGES, Gson().toJson(tempUser.spoken_languages))
                .append(KEYS.GAMES_LIST, Gson().toJson(tempUser.games_list))
                .append(KEYS.FRIENDS_LIST, Gson().toJson(tempUser.friends_list))
                .append(KEYS.FRIEND_REQUESTS, Gson().toJson(tempUser.friend_requests))
                .append(
                    MongoTools.KEYS.LIST_OF_CONVERSATIONS,
                    Gson().toJson(tempUser.list_of_conversations)
                )
        )
            .getAsync { result ->
                if (result.isSuccess) {
                    Log.v(
                        TAG,
                        "Inserted custom user data document. _id of inserted document: ${result.get().insertedId}"
                    )
                    resultListener.getResult(
                        true,
                        "Inserted custom user data document. _id of inserted document: ${result.get().insertedId}"
                    )
                } else {
                    Log.e(
                        TAG,
                        "Unable to insert custom user data. Error: ${result.error}"
                    )
                    resultListener.getResult(
                        false,
                        "Unable to insert custom user data. Error: ${result.error}"
                    )
                }
            }
    }


    fun fetchUsersFromDB(searchQuery: String) {
        Log.d(TAG, "fetchUsersFromDB: fetching: $searchQuery")
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase("gamenet_users")!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection("custom_data")!!
        val query1 = Document("\$regex", ".*$searchQuery.*")
        val query2 = Document(MongoTools.KEYS.USER_NAME, query1)
        val cursor = mongoCollection.find(query2)
        val resultList = arrayListOf<AppUser>()
        val iterator = cursor.iterator().getAsync { result ->
            if (result != null) {
                while (result.get().hasNext()) {
                    val temp = result.get().next()
                    Log.d(TAG, "onResult: $temp")
                    resultList.add(parseUser(temp))
                }
                resultListener.getQueriedUsers(true, "All good", resultList)
            } else {
                resultListener.getQueriedUsers(false, "Result is null", arrayListOf())
            }
        }
    }

    private fun parseUser(temp: Document?): AppUser {
        Log.d(TAG, "parseUser: $temp")
        val tempUser = AppUser()
        if (temp != null) {
            val plainJson = JSONObject(temp.toJson())
            tempUser.user_id = plainJson.get(KEYS.USER_ID) as String
            tempUser.username = plainJson.get(KEYS.USER_NAME) as String
            tempUser.first_name = plainJson.get(KEYS.FIRST_NAME) as String
            tempUser.last_name = plainJson.get(KEYS.LAST_NAME) as String
            tempUser.email = plainJson.get(KEYS.EMAIL) as String
            tempUser.country = plainJson.get(KEYS.COUNTRY) as String
            tempUser.gender = plainJson.get(KEYS.GENDER) as String
            tempUser.photo_url = plainJson.get(KEYS.PHOTO_URL) as String
            tempUser.spoken_languages = Gson().fromJson(
                plainJson.get(KEYS.SPOKEN_LANGUAGES) as String, ArrayList::class.java
            ) as ArrayList<String>
            val gameType: Type = object : TypeToken<ArrayList<Game>>() {}.type
            tempUser.games_list = Gson().fromJson(
                plainJson.get(KEYS.GAMES_LIST) as String, gameType
            ) as ArrayList<Game>
            val userType: Type = object : TypeToken<ArrayList<AppUser>>() {}.type
            tempUser.friends_list = Gson().fromJson(
                plainJson.get(KEYS.FRIENDS_LIST) as String, userType
            ) as ArrayList<AppUser>
            val conversationType: Type = object : TypeToken<ArrayList<Conversation>>() {}.type
            tempUser.list_of_conversations = Gson().fromJson(
                plainJson.get(KEYS.LIST_OF_CONVERSATIONS) as String, conversationType
            ) as ArrayList<Conversation>
            val friendRequestsType: Type = object : TypeToken<ArrayList<FriendRequest>>() {}.type
            tempUser.friend_requests = Gson().fromJson(
                plainJson.get(KEYS.FRIEND_REQUESTS) as String,
                friendRequestsType
            ) as ArrayList<FriendRequest>
        }
        return tempUser
    }
}