package com.vadim.gamenet.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.adapters.GameListAdapter
import com.vadim.gamenet.adapters.LanguageSelectionAdapter
import com.vadim.gamenet.fragments.FragmentProfile
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.FriendRequest
import com.vadim.gamenet.utils.MongoTools
import org.bson.Document

class DialogUserDetails(context: Context, targetUser: AppUser, srcUser: AppUser, mode: Int) :
    Dialog(context) {


    object MODE {
        const val FRIEND_LIST = 0
        const val PROFILE_PAGE = 1
    }

    private val targetUser = targetUser
    private val srcUser = srcUser
    private val mode = mode
    private val mongoUser = MyAppClass.Constants.app.currentUser()
    private lateinit var profilePictureImg: ShapeableImageView
    private lateinit var userNameLbl: TextView
    private lateinit var fullnameLbl: TextView
    private lateinit var genderCountryLbl: TextView
    private lateinit var languageListRecycler: RecyclerView
    private lateinit var gamesListRecycler: RecyclerView
    private lateinit var requestBtn: MaterialButton
    private lateinit var cancelBtn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_user_details)
        initViews()
    }

    private fun initViews() {
        Log.d(TAG, "initViews: ")
        profilePictureImg = findViewById(R.id.dialogDetails_IMG_profileImage)
        Glide.with(context).load(targetUser.photo_url).into(profilePictureImg)
        userNameLbl = findViewById(R.id.dialogDetails_LBL_userName)
        userNameLbl.text = targetUser.username
        fullnameLbl = findViewById(R.id.dialogDetails_LBL_fullName)
        fullnameLbl.text = "${targetUser.first_name} ${targetUser.last_name}"
        genderCountryLbl = findViewById(R.id.dialogDetails_LBL_genderCountry)
        genderCountryLbl.text = "${targetUser.gender}, ${targetUser.country}"
        languageListRecycler = findViewById(R.id.dialogDetails_LST_languages)
        Log.d(TAG, "initViews: sending: Languages ${targetUser.spoken_languages}")
        languageListRecycler.adapter =
            LanguageSelectionAdapter(context, targetUser.spoken_languages)
        gamesListRecycler = findViewById(R.id.dialogDetails_LST_games)
        Log.d(TAG, "initViews: sending: Games ${targetUser.games_list}")
        gamesListRecycler.adapter = GameListAdapter(context, targetUser.games_list)
        requestBtn = findViewById(R.id.dialogDetails_BTN_add)
        cancelBtn = findViewById(R.id.dialogDetails_BTN_cancel)



        if (mode == MODE.FRIEND_LIST) {
            Log.d(TAG, "initViews: friend list mode")

            val id = srcUser.email + "request" + targetUser.email


            if (!srcUser.friends_list.contains(targetUser.email)) { // is not my friend
                requestBtn.setOnClickListener {
                    sendRequest(
                        FriendRequest(
                            id,
                            srcUser.email,
                            targetUser.email
                        )
                    ) // add request params
                }
            } else { // if my friend
                requestBtn.text = context.resources.getString(R.string.message)
                requestBtn.setOnClickListener {
                    sendMessage(srcUser, targetUser)
                }
            }
            cancelBtn.setOnClickListener {
                this.dismiss() // dismiss dialog
            }

        } else {
            Log.d(TAG, "initViews: profile page mode")
            requestBtn.text = context.resources.getString(R.string.approve)
            requestBtn.setOnClickListener {
                approveFriend()
            }
            cancelBtn.text = context.resources.getString(R.string.decline)
            cancelBtn.setOnClickListener {
                declineFriend()
                this.dismiss() // dismiss dialog
            }
        }
    }

    private fun sendMessage(from: AppUser, to: AppUser) {
        Log.d(TAG, "sendMessage: $from -> $to")


    }

    private fun declineFriend() {
        Log.d(TAG, "declineFriend: ")
        //TODO: Refresh request list after
        val tools = mongoUser?.let {
            MongoTools(context, it, object : MongoTools.ResultListener {
                override fun getResult(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "getResult: success: $message")
                    } else {
                        Log.d(TAG, "getResult: error: $message")
                    }
                }
            })
        }
        val user = MyAppClass.Constants.app.currentUser()
        val query = "${targetUser.email}request${srcUser.email}"
        tools?.deleteDocumentFromCollection(
            user, "gamenet_users",
            "friend_requests", query
        )
    }

    private fun approveFriend() {
        Log.d(TAG, "approveFriend: ")

        srcUser.friends_list.add(targetUser.email) // TODO: add emails
        targetUser.friends_list.add(srcUser.email)
        Log.d(
            TAG,
            "approveFriend: myJson: ${srcUser.friends_list} hisJson: ${targetUser.friends_list}"
        )
        val srcFriendList = srcUser.friends_list // me
        val targetFriendList = targetUser.friends_list // the sender

        val me = srcUser.email // me
        val otherUser = targetUser.email // sender

        if (mongoUser != null) {
            val tools = MongoTools(context, mongoUser, object : MongoTools.ResultListener {
                override fun getResult(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "getResult: success adding: $message")
                    } else {
                        Log.d(TAG, "getResult: error adding: $message")
                    }
                }
            })
            tools.saveUserCustomData(
                MongoTools.USER_KEYS.FRIENDS_LIST,
                Gson().toJson(srcFriendList).toString(),
                me
            )
            tools.saveUserCustomData(
                MongoTools.USER_KEYS.FRIENDS_LIST,
                Gson().toJson(targetFriendList).toString(),
                otherUser
            )
        }
    }

    private fun sendRequest(friendRequest: FriendRequest) {
        Log.d(TAG, "sendRequest: ")
        val user = MyAppClass.Constants.app.currentUser()
        val tools2 = user?.let {
            MongoTools(context, it, object : MongoTools.ResultListener { // add request tool
                override fun getResult(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "getResult SUCCESS: $message")

                    } else {
                        Log.d(TAG, "getResult ERROR: $message")
                    }
                }
            })
        }
        val tools = user?.let {
            MongoTools(context, it, object : MongoTools.ResultListener { // existing document tool
                override fun getResult(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "getResult TRUE: $message")
                        Toast.makeText(
                            context,
                            "You already sent a request to this user!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.d(TAG, "getResult FALSE: $message")
                        tools2?.addDocumentToDatabase(
                            user,
                            "gamenet_users",
                            "friend_requests",
                            MongoTools.TYPE.FRIEND_REQUEST,
                            Gson().toJson(friendRequest).toString()
                        )
                    }
                }
            })
        }

        if (user != null) {
            val searchQuery = srcUser.email + "request" + targetUser.email
            val query2 = Document("id", searchQuery)
            tools?.checkIfDocumentExists(user, "gamenet_users", "friend_requests", query2)
        }
    }
}