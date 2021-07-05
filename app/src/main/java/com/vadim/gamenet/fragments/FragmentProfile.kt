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
import com.vadim.gamenet.adapters.GameListAdapter
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.utils.FirebaseTools
import com.vadim.gamenet.utils.MongoTools
import io.realm.mongodb.App

class FragmentProfile(myUser: AppUser) : Fragment() {

    private val myUser = myUser
    private val app: App = MyAppClass.Constants.app
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var userNameLbl: TextView
    private lateinit var fullNameLbl: TextView
    private lateinit var gamesListView: RecyclerView
    private lateinit var messageListView: RecyclerView

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
        mongoTools?.saveUserCustomData(MongoTools.KEYS.PHOTO_URL, imageUrl, myUser.email)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView with user:$myUser ")
        val mView = inflater.inflate(R.layout.fragment_profile, container, false)
        initViews(mView)
        return mView;
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
        messageListView = mView.findViewById(R.id.fragmentProfile_LST_messages)
    }

    private fun checkForPermissions(STORAGE_PERMISSION_CODE: Int): Boolean {
        Log.d(TAG, "checkForPermissions: ")
        return true
    }
}