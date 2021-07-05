package com.vadim.gamenet.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.adapters.MainFeedAdapter
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.FeedPost
import io.realm.mongodb.App
import io.realm.mongodb.mongo.MongoClient
import io.realm.mongodb.mongo.MongoCollection
import io.realm.mongodb.mongo.MongoDatabase
import org.bson.Document
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class FragmentMainFeed(myUser: AppUser) : Fragment() {

    private lateinit var addPostButton: FloatingActionButton
    private lateinit var mainFeedList: RecyclerView
    private lateinit var app: App
    private val mainFeedPosts: ArrayList<FeedPost> = arrayListOf()
    private lateinit var mongoClient: MongoClient
    private lateinit var mongoDatabase: MongoDatabase
    private lateinit var mongoCollection: MongoCollection<Document>
    private val myUser = myUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        val mView = inflater.inflate(R.layout.fragment_main_feed, container, false)
        app = MyAppClass.Constants.app
        initViews(mView)
        Thread {
            populateFeed()
        }.start()
        return mView;
    }

    private fun populateFeed() {
        Log.d(TAG, "populateFeed: ${app.currentUser()?.id}")
        val mongoClient: MongoClient =
            app.currentUser()
                ?.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase("feed_db")!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection("posts")!!
        val itemsIterator = mongoCollection.find().iterator().get()
        while (itemsIterator.hasNext()) {
            val temp = itemsIterator.next().toJson().toString()
            mainFeedPosts.add(parseDocument(temp))
        }
        requireActivity().runOnUiThread {
            refreshPostsUI()
        }
    }

    private fun parseDocument(document: String): FeedPost {
        Log.d(TAG, "parseDocument: $document")
        val tempPost = FeedPost()
        val tempPostJson = JSONObject(document)
        tempPost.id = (tempPostJson.get("_id") as JSONObject).get("\$oid") as String
        tempPost.comments = Gson()
            .fromJson(
                tempPostJson.get("comments") as String,
                ArrayList::class.java
            ) as ArrayList<String>
        tempPost.userId = tempPostJson.get("user") as String
        tempPost.userName = tempPostJson.get("username") as String
        tempPost.profilePictureUri = tempPostJson.get("image_uri") as String
        tempPost.likeCount = tempPostJson.get("like_count") as Int
        tempPost.message = tempPostJson.get("content") as String
        tempPost.postTime =
            ((tempPostJson.get("post_time") as JSONObject).get("\$numberLong") as String).toLong()
        return tempPost
    }

    /**
     * A method to initialize all the views
     */
    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: ")
        addPostButton = mView.findViewById(R.id.mainFeed_BTN_addPostButton)
        mainFeedList = mView.findViewById(R.id.mainFeed_LST_postList)
        addPostButton.setOnClickListener {
            addPost();
        }
    }

    private fun addPost() {
        Log.d(TAG, "addPost: ")
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val width = (resources.displayMetrics.widthPixels * 0.8).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.7).toInt()
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_new_post)
        val contentLayout =
            dialog.findViewById(R.id.newPost_EDT_postContentLayout) as TextInputLayout
        val contentEdt = dialog.findViewById(R.id.newPost_EDT_postContentEdt) as TextInputEditText
        val submitBtn = dialog.findViewById(R.id.newPost_EDT_submit) as MaterialButton
        submitBtn.setOnClickListener {
            val content = contentEdt.text.toString()
            Log.d(TAG, "addPost content = $content")
            addNewPost(content)
            dialog.dismiss()
        }
        dialog.show()
        dialog.window!!.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL)
    }

    private fun addNewPost(content: String) {
        Log.d(TAG, "addNewPost: ")
        mongoClient = app.currentUser()?.getMongoClient("mongodb-atlas")!!
        mongoDatabase = mongoClient.getDatabase("feed_db")!!
        mongoCollection = mongoDatabase.getCollection("posts")!!
        val tempPost = app.currentUser()?.id?.let {
            FeedPost(
                UUID.randomUUID().toString(),
                it,
                myUser.username,
                myUser.photo_url,
                0,
                content,
                arrayListOf(),
                System.currentTimeMillis()
            )
        }

        if (tempPost != null) {
            mongoCollection.insertOne(
                Document("post_id", tempPost.id)
                    .append("user", app.currentUser()?.id)
                    .append("username", myUser.username)
                    .append("image_uri", myUser.photo_url)
                    .append("like_count", tempPost.likeCount)
                    .append("content", tempPost.message)
                    .append("post_time", tempPost.postTime)
                    .append("comments", Gson().toJson(arrayListOf<String>()))
            )
                .getAsync { result ->
                    if (result.isSuccess) {
                        Log.v(
                            TAG,
                            "Inserted custom user data document. _id of inserted document: ${result.get().insertedId}"
                        )
                        mainFeedPosts.add(FeedPost())
                        requireActivity().runOnUiThread {
                            refreshPostsUI()
                        }
                    } else {
                        Log.e(
                            TAG,
                            "Unable to insert custom user data. Error: ${result.error}"
                        )
                        //TODO:Handle problems with details
                    }
                }
        }
    }

    private fun refreshPostsUI() {
        Log.d(TAG, "refreshPostsUI: ")
        mainFeedPosts.sortDescending()
        val adapter = MainFeedAdapter(requireContext(), mainFeedPosts)
        mainFeedList.adapter = adapter
    }
}