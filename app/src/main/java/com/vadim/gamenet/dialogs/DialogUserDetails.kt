package com.vadim.gamenet.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.TextView
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
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.FriendRequest
import com.vadim.gamenet.utils.MongoTools
import java.util.*
import kotlin.collections.ArrayList

class DialogUserDetails(context: Context, targetUser: AppUser, srcUser: AppUser) : Dialog(context) {

    private val targetUser = targetUser
    private val srcUser = srcUser
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
        requestBtn.setOnClickListener {
            sendRequest(FriendRequest(UUID.randomUUID().toString(), srcUser)) // add request params
        }
        cancelBtn = findViewById(R.id.dialogDetails_BTN_cancel)
        cancelBtn.setOnClickListener {
            this.dismiss() // dismiss dialog
        }
    }

    private fun sendRequest(friendRequest: FriendRequest) {
        Log.d(TAG, "sendRequest: ")
        targetUser.friend_requests.add(friendRequest)
        val tools = MyAppClass.Constants.app.currentUser()?.let {
            MongoTools(context,
                it, object : MongoTools.ResultListener {
                    override fun getResult(result: Boolean, message: String) {
                        if (result) {
                            Log.d(TAG, "getResult: Success: $message")
                        } else {
                            Log.d(TAG, "getResult: Error: $message")
                        }
                    }

                    override fun getQueriedUsers(
                        result: Boolean,
                        message: String,
                        userList: ArrayList<AppUser>
                    ) {
                        Log.d(TAG, "getQueriedUsers: ")
                    }
                })
        }
        tools?.saveUserCustomData(
            MongoTools.KEYS.FRIEND_REQUESTS,
            Gson().toJson(targetUser.friend_requests),
            targetUser.email
        )
    }
}