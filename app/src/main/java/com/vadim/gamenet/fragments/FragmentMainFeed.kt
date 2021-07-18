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
import com.vadim.gamenet.utils.MongoTools
import com.vadim.gamenet.utils.ParsingTools
import io.realm.mongodb.mongo.MongoClient
import io.realm.mongodb.mongo.MongoCollection
import io.realm.mongodb.mongo.MongoDatabase
import org.bson.Document
import org.json.JSONArray
import kotlin.collections.ArrayList


class FragmentMainFeed(myUser: AppUser) : Fragment() {

    private lateinit var addPostButton: FloatingActionButton
    private lateinit var mainFeedList: RecyclerView
    private val app = MyAppClass.Constants.app
    private val mongoUser = app.currentUser()
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
        initViews(mView)
        populateFeed()
        return mView;
    }

    private fun populateFeed() {
        Log.d(TAG, "populateFeed: ${app.currentUser()?.id}")
        if (mongoUser != null) {
            val tools = MongoTools(requireContext(), mongoUser, object : MongoTools.ResultListener {
                override fun getResult(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "populateFeed: SUCCESS: $message")
                        val plainJsonArray = JSONArray(message)
                        for (i in 0 until plainJsonArray.length()) {
                            val tempPost =
                                ParsingTools.parsePost(plainJsonArray.get(i).toString())
                            mainFeedPosts.add(tempPost)
                            requireActivity().runOnUiThread {
                                refreshPostsUI()
                            }
                        }
                    } else {
                        Log.d(TAG, "populateFeed: FAILURE: $message")
                    }
                }
            })
            tools.fetchDocumentFromDatabase(
                mongoUser, "feed_db", "posts",
                Document("find", "any")
            )
        }
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
        val post = FeedPost()
        val tools = mongoUser?.let {
            MongoTools(requireContext(), it, object : MongoTools.ResultListener {
                override fun getResult(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "getResult add document: SUCCESS: $message")
                        mainFeedPosts.add(post)
                        requireActivity().runOnUiThread {
                            refreshPostsUI()
                        }
                    } else {
                        Log.d(TAG, "getResult add document: FAILURE: $message")
                    }
                }
            })
        }
        post.user_id = myUser.user_id
        post.username = myUser.username
        post.profile_picture_uri = myUser.photo_url
        post.content = content

        if (mongoUser != null) {
            tools?.addDocumentToDatabase(
                mongoUser,
                "feed_db",
                "posts",
                MongoTools.TYPE.POST,
                Gson().toJson(post)
            )
        }
    }

    private fun refreshPostsUI() {
        Log.d(TAG, "refreshPostsUI: ")
        mainFeedPosts.sortDescending()
        val adapter = MainFeedAdapter(requireContext(), mainFeedPosts)
        mainFeedList.adapter = adapter
    }
}