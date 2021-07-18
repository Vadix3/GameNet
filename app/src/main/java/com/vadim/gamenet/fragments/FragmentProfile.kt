package com.vadim.gamenet.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.STORAGE_PERMISSION_CODE
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.adapters.FriendsListAdapter
import com.vadim.gamenet.adapters.GameListAdapter
import com.vadim.gamenet.dialogs.DialogUserDetails
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.FriendRequest
import com.vadim.gamenet.models.Message
import com.vadim.gamenet.utils.FirebaseTools
import com.vadim.gamenet.utils.MongoTools
import com.vadim.gamenet.utils.ParsingTools
import io.realm.mongodb.App
import org.bson.Document
import org.json.JSONArray

class FragmentProfile(myUser: AppUser) : Fragment() {

    private val myUser = myUser
    private val app: App = MyAppClass.Constants.app
    private val mongoAppUser = app.currentUser()
    private val messagesList = arrayListOf<Message>()
    private val requestList = arrayListOf<FriendRequest>()
    private val requestingFriends = arrayListOf<AppUser>()
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var userNameLbl: TextView
    private lateinit var fullNameLbl: TextView
    private lateinit var messagesListView: RecyclerView
    private lateinit var requestsListView: RecyclerView

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val imageUri = data.data
                    val firebaseTools = FirebaseTools(requireContext(),
                        object : FirebaseTools.UploadResultListener {
                            override fun getUploadResult(result: Boolean, imageUrl: String?) {
                                Log.d(TAG, "getUploadResult: ")
                                if (result) {
                                    Log.d(TAG, "getUploadResult: $imageUrl")
                                    if (imageUrl != null) {
                                        myUser.photo_url = imageUrl
                                        updateUserInStorage(imageUrl)
                                        requireActivity().runOnUiThread {
                                            Glide.with(requireContext()).load(imageUrl)
                                                .into(profileImageView)
                                        }
                                    }
                                } else {
                                    Log.d(TAG, "getUploadResult: Error: $imageUrl")
                                    //TODO: Handle errors
                                }
                            }

                            override fun getDownloadResult(result: Boolean, imageUrl: String?) {
                                Log.d(TAG, "getDownloadResult: ")
                            }
                        })
                    if (imageUri != null) {
                        firebaseTools.putImageIntoStorage(imageUri, myUser.user_id)
                    }
                }
            }
        }

    private fun updateUserInStorage(imageUrl: String) {
        Log.d(TAG, "updateUserInStorage: ")
        val mongoUser = app.currentUser()
        val mongoTools = mongoUser?.let {
            MongoTools(requireContext(),
                it,
                object : MongoTools.ResultListener {
                    override fun getResult(result: Boolean, message: String) {
                        if (result) {
                            Log.d(TAG, "getResult: Success: $message")

                        } else {
                            Log.d(TAG, "getResult: Error: $message")
                        }
                    }
                })
        }
        mongoTools?.saveUserCustomData(
            MongoTools.USER_KEYS.PHOTO_URL,
            imageUrl,
            myUser.email
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView with user:$myUser ")
        val mView = inflater.inflate(R.layout.fragment_profile, container, false)
        initViews(mView)
        fetchMessages()
        fetchRequests()
        return mView;
    }

    /** A method to fetch the requests for the current user*/
    private fun fetchRequests() {
        Log.d(TAG, "fetchRequests: ")
        val user = app.currentUser()
        if (user != null) {
            val tools = app.currentUser()?.let {
                MongoTools(requireContext(), it, object : MongoTools.ResultListener {
                    override fun getResult(result: Boolean, message: String) {
                        if (result && (message != "[]")) {
                            Log.d(TAG, "getResult: SUCCESS fetch requests: $message")
                            val requestsJson = JSONArray(message)
                            for (i in 0 until requestsJson.length()) {
                                Log.d(TAG, "getResult: ${requestsJson[i]}")
                                val tempRequest =
                                    ParsingTools.parseFriendRequest(requestsJson[i].toString())
                                requestList.add(tempRequest)
                                fetchRequestingUsers()
                            }
                            Log.d(TAG, "getResult: request list: $requestList")
                        } else {
                            Log.d(TAG, "getResult: ERROR: $message")
                        }
                    }

                })
            }
            val query = Document(MongoTools.FRIEND_REQUEST_KEYS.RECEIVER, myUser.email)
            Log.d(TAG, "fetchRequests: fetching document with query:$query")
            tools?.fetchDocumentFromDatabase(user, "gamenet_users", "friend_requests", query)
        }
    }

    /** A method to fetch the users that request friendship*/
    private fun fetchRequestingUsers() {
        Log.d(TAG, "fetchRequestingUsers: ")
        if (mongoAppUser != null) {
            val tools =
                MongoTools(requireContext(), mongoAppUser, object : MongoTools.ResultListener {
                    override fun getResult(result: Boolean, message: String) {
                        if (result && message != "[]") {
                            Log.d(TAG, "getResult: SUCCESS got user: $message")
                            val resultJson = JSONArray(message)
                            for (i in 0 until resultJson.length()) {
                                val tempUser = ParsingTools.parseUser(resultJson[i].toString())
                                Log.d(TAG, "getResult: temp user = $tempUser")
                                requestingFriends.add(tempUser)
                            }
                            Log.d(TAG, "getResult: requesting friends: $requestingFriends")
                            requireActivity().runOnUiThread {
                                val adapter =
                                    FriendsListAdapter(
                                        requireContext(),
                                        requestingFriends,
                                        myUser,
                                        DialogUserDetails.MODE.PROFILE_PAGE
                                    )
                                requestsListView.adapter = adapter
                            }
                        } else {
                            Log.d(TAG, "getResult: FAILURE: $message")
                        }
                    }
                })
            for (i in requestList) {
                val tempQuery = i.sender
                val query = Document(MongoTools.USER_KEYS.EMAIL, tempQuery)
                tools.fetchDocumentFromDatabase(mongoAppUser, "gamenet_users", "custom_data", query)
            }
        }
    }

    private fun fetchMessages() {
        Log.d(TAG, "fetchMessages: ")
    }


    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: ")
        profileImageView = mView.findViewById(R.id.fragmentProfile_IMG_profileImage)
        Glide.with(requireActivity()).load(myUser.photo_url).into(profileImageView)
        profileImageView.setOnClickListener {
            if (checkForPermissions(STORAGE_PERMISSION_CODE)) {
                MyAppClass.openStorage(resultLauncher)
            }
        }
        userNameLbl = mView.findViewById(R.id.fragmentProfile_LBL_userName)
        userNameLbl.text = myUser.username
        fullNameLbl = mView.findViewById(R.id.fragmentProfile_LBL_fullName)
        fullNameLbl.text = "${myUser.first_name.capitalize()} ${myUser.last_name.capitalize()}"

        messagesListView = mView.findViewById(R.id.fragmentProfile_LST_messages)
        requestsListView = mView.findViewById(R.id.fragmentProfile_LST_requests)
    }

    private fun checkForPermissions(storagePermissionCode: Int): Boolean {
        return true
    }
}