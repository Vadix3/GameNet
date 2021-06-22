package com.vadim.gamenet.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R

class FragmentMainFeed : Fragment() {

    private lateinit var addPostButton: FloatingActionButton
    private lateinit var mainFeedList: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MyAppClass.Constants.TAG, "onCreateView: ")
        val mView = inflater.inflate(R.layout.fragment_main_feed, container, false);

        initViews(mView);

        return mView;
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
    }
}