package com.vadim.gamenet.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.models.AppUser

class FragmentChat(myUser: AppUser) : Fragment() {

    private val myUser = myUser
    private lateinit var createConversationBtn: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MyAppClass.Constants.TAG, "onCreateView: ")
        val mView = inflater.inflate(R.layout.fragment_chat, container, false)
        initViews(mView)
        return mView;
    }

    private fun initViews(mView: View?) {
        Log.d(TAG, "initViews: ")
        if (mView != null) {
            createConversationBtn = mView.findViewById(R.id.chat_BTN_addConversation)
            createConversationBtn.setOnClickListener {
                addConversation()
            }
        }
    }

    private fun addConversation() {
        Log.d(TAG, "addConversation: ")

    }
}