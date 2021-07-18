package com.vadim.gamenet.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.MyAppClass.Constants.app
import com.vadim.gamenet.R
import com.vadim.gamenet.adapters.FriendsListAdapter
import com.vadim.gamenet.dialogs.DialogUserDetails
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.utils.MongoTools
import com.vadim.gamenet.utils.ParsingTools
import org.bson.Document
import org.json.JSONArray

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
        val myFriends = myUser.friends_list
        if (mongoUser != null) {
            val tools = MongoTools(requireContext(), mongoUser, object : MongoTools.ResultListener {
                override fun getResult(result: Boolean, message: kotlin.String) {
                    if (result) {
                        Log.d(TAG, "getResult: SUCCESS: $message")
                        val tempUser = ParsingTools.parseUser(JSONArray(message)[0].toString())
                        friendsList.add(tempUser)
                        requireActivity().runOnUiThread {
                            refreshFriendsList()
                        }

                    } else {
                        Log.d(TAG, "getResult: ERROR: $message")
                    }
                }
            })
            for (tempEmail in myFriends) {
                val query = Document(MongoTools.USER_KEYS.EMAIL, tempEmail)
                tools.fetchDocumentFromDatabase(mongoUser, "gamenet_users", "custom_data", query)
            }
        }
    }

    private fun initViews(mView: View?) {
        Log.d(TAG, "initViews: ")
        if (mView != null) {
            friendsListRecycler = mView.findViewById(R.id.friends_LST_friendsList)
        }
    }

    private fun refreshFriendsList() {
        Log.d(TAG, "refreshFriendsList: ")
        val adapter = FriendsListAdapter(
            requireContext(),
            friendsList,
            myUser,
            DialogUserDetails.MODE.FRIEND_LIST
        )
        friendsListRecycler.adapter = adapter
    }
}