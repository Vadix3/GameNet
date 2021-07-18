package com.vadim.gamenet.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.models.*
import io.realm.mongodb.User
import io.realm.mongodb.mongo.MongoClient
import io.realm.mongodb.mongo.MongoCollection
import io.realm.mongodb.mongo.MongoDatabase
import io.realm.mongodb.mongo.iterable.FindIterable
import org.bson.Document

class MongoTools(context: Context, user: User, resultListener: ResultListener) {
    private val context = context

    object USER_KEYS {
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

    object FRIEND_REQUEST_KEYS {
        const val ID = "id"
        const val RECEIVER = "receiver"
        const val SENDER = "sender"
        const val TIME = "time"
    }

    object TYPE {
        const val CUSTOM_DATA = 1
        const val FRIEND_REQUEST = 2
        const val CONVERSATION = 3
        const val POST = 4
    }

    private val resultListener = resultListener
    private val mongoUser = user
    private val mongoClient: MongoClient =
        mongoUser.getMongoClient("mongodb-atlas")!!

    interface ResultListener {
        fun getResult(result: Boolean, message: String)
    }

    fun checkIfDocumentExists(user: User, database: String, collection: String, query: Document) {
        Log.d(TAG, "checkIfDocumentExists: ")
        val mongoClient: MongoClient =
            user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase(database)!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection(collection)!!
        val cursor = mongoCollection.findOne(query).getAsync { result ->
            Log.d(TAG, "onResult: result = ${result.get()}")
            if (result.get() == null) {
                Log.d(TAG, "checkIfDocumentExists: document does not exist")
                resultListener.getResult(false, "ERROR: Document does not exist")
            } else {
                Log.d(TAG, "checkIfDocumentExists: document exists")
                resultListener.getResult(true, "Document exists! id:${result}")
            }
        }
    }


    fun addItemToArray(
        database: String,
        collection: String,
        key: String,
        id: String,
        listName: String,
        value: String
    ) {
        Log.d(TAG, "addItemToArray: $key $id $listName $value")

//        db.students.update(
//            { _id: 1 },
//            { $push: { scores: 89 } }
//        )

        val query = Document(key, id)
        val updateValue = Document("\$push", Document(listName, value))
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase(database)!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection(collection)!!

        mongoCollection.updateOne(query, updateValue).getAsync { task ->
            if (task.isSuccess) {
                val count = task.get().modifiedCount
                if (count == 1L) {
                    resultListener.getResult(true, "Successfully added the item")
                } else {
                    Log.v(TAG, "did not update a document.")
                    resultListener.getResult(false, "did not add: ${task.error}")
                }
            } else {
                Log.e(TAG, "failed to update document with: ${task.error}")
            }
        }


    }

    fun saveUserCustomData(
        key: String, value: String, userEmail: String
    ) {
        Log.d(TAG, "saveUserCustomData: Key: $key Value: $value userEmail: $userEmail")
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase("gamenet_users")!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection("custom_data")!!
        val queryFilter = Document(USER_KEYS.EMAIL, userEmail)
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


    /** A function to fetch a document from given query in the following way:
    val searchQuery = "Vad"
    val query1 = Document("\$regex", ".*$searchQuery.*")
    val query2 = Document(MongoTools.KEYS.USER_NAME, query1)
     */

    fun fetchDocumentFromDatabase(
        user: User,
        database: String,
        collection: String,
        query: Document
    ) {
        Log.d(TAG, "fetchDocumentFromDatabase: $database $collection $query")
        val mongoClient: MongoClient =
            user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase(database)!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection(collection)!!
        val cursor: FindIterable<Document> = if (query == Document("find", "any")) {
            Log.d(TAG, "fetchDocumentFromDatabase: searching all")
            mongoCollection.find()
        } else {
            mongoCollection.find(query)
        }
        val resultsArray = arrayListOf<String>()
        context.run {
            val iterator = cursor.iterator().getAsync { result ->
                if (result != null) {
                    while (result.get().hasNext()) {
                        val temp = result.get().next()
                        Log.d(TAG, "onResult: $temp")
                        resultsArray.add(temp.toJson())
                    }
                    resultListener.getResult(true, resultsArray.toString())
                } else {
                    resultListener.getResult(false, "Result array is null")
                }
            }
        }
    }


    fun enableDocumentListener(database: String, collection: String, key: String, value: String) {
        Log.d(TAG, "enableDocumentListener: ")
        val mongoClient: MongoClient =
            mongoUser.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase(database)!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection(collection)!!
        val target = Document(key, value)
//        val watcher = mongoCollection
//            .watchWithFilterAsync(target)
        val watcher = mongoCollection.watchAsync()
        Log.d(TAG, "enableDocumentListener: watching: $target")

        watcher[{ result ->
            if (result.isSuccess) {
                Log.v(
                    TAG,
                    "Event type: ${result.get().operationType} full document: ${result.get().fullDocument}"
                )
                result.get().fullDocument?.let { resultListener.getResult(true, it.toJson()) }

            } else {
                Log.e(
                    TAG,
                    "failed to subscribe to changes in the collection with : ${result.error}"
                )
                resultListener.getResult(false, result.error.toString())
            }
        }]


    }

    fun addDocumentToDatabase(
        user: User,
        database: String,
        collection: String,
        type: Int,
        content: String
    ) {
        Log.d(TAG, "addDocumentToDatabase: $database $collection $type $content")
        val mongoClient: MongoClient =
            user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase(database)!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection(collection)!!
        mongoCollection.insertOne(Document.parse(content))
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
            Document(USER_KEYS.USER_ID, user.id)
                .append(USER_KEYS.USER_NAME, tempUser.username)
                .append(USER_KEYS.FIRST_NAME, tempUser.first_name)
                .append(USER_KEYS.LAST_NAME, tempUser.last_name)
                .append(USER_KEYS.EMAIL, tempUser.email)
                .append(USER_KEYS.COUNTRY, tempUser.country)
                .append(USER_KEYS.GENDER, tempUser.gender)
                .append(USER_KEYS.PHOTO_URL, tempUser.photo_url)
                .append(USER_KEYS.SPOKEN_LANGUAGES, Gson().toJson(tempUser.spoken_languages))
                .append(USER_KEYS.GAMES_LIST, Gson().toJson(tempUser.games_list))
                .append(USER_KEYS.FRIENDS_LIST, Gson().toJson(tempUser.friends_list))
                .append(
                    USER_KEYS.LIST_OF_CONVERSATIONS,
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

    fun deleteDocumentFromCollection(
        user: User?,
        database: String,
        collection: String,
        query: String
    ) {
        Log.d(TAG, "deleteDocumentFromCollection: deleting: ")
        if (user != null) {
            val mongoClient: MongoClient =
                user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
            val mongoDatabase: MongoDatabase =
                mongoClient.getDatabase(database)!!
            val mongoCollection: MongoCollection<Document> =
                mongoDatabase.getCollection(collection)!!
            mongoCollection.deleteOne(Document("id", query)).getAsync { result ->
                if (result.isSuccess) {
                    resultListener.getResult(true, result.toString())
                } else {
                    resultListener.getResult(false, result.error.toString())
                }
            }
        }


    }
}