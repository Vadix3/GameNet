package com.vadim.gamenet.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.MyAppClass.Constants.app
import com.vadim.gamenet.R
import com.vadim.gamenet.adapters.FriendsListAdapter
import com.vadim.gamenet.adapters.MainFeedAdapter
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.utils.MongoTools

class FragmentFriends(myUser: AppUser) : Fragment() {

    private val myUser = myUser
    private var friendsList: ArrayList<AppUser> = arrayListOf()
    private lateinit var friendsListRecycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        val mView = inflater.inflate(R.layout.fragment_friends, container, false)
        initViews(mView)
        fetchUsersFromDB()
        return mView;
    }

    private fun fetchUsersFromDB() {
        Log.d(TAG, "fetchUsersFromDB: ")
        val mongoUser = app.currentUser()
        val tools = mongoUser?.let {
            MongoTools(requireContext(),
                it, object : MongoTools.ResultListener {
                    override fun getResult(result: Boolean, message: String) {
                    }

                    override fun getQueriedUsers(
                        result: Boolean,
                        message: String,
                        userList: ArrayList<AppUser>
                    ) {
                        if (result) {
                            Log.d(TAG, "getQueriedUsers: Got: $userList")
                            friendsList = userList
                            requireActivity().runOnUiThread {
                                refreshFriendsList()
                            }
                        } else {
                            Log.d(TAG, "getQueriedUsers: Error: $message")
                        }
                    }

                })
        }
        tools?.fetchUsersFromDB("Vad")
    }

    private fun initViews(mView: View?) {
        Log.d(TAG, "initViews: ")
        if (mView != null) {
            friendsListRecycler = mView.findViewById(R.id.friends_LST_friendsList)
        }
    }

    private fun refreshFriendsList() {
        Log.d(TAG, "refreshFriendsList: ")
        val adapter = FriendsListAdapter(requireContext(), friendsList,myUser)
        friendsListRecycler.adapter = adapter
    }
}