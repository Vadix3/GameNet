package com.vadim.gamenet.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.R

class FragmentHelloApi : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MyAppClass.Constants.TAG, "onCreateView: ")
        val mView = inflater.inflate(R.layout.fragment_hello_api, container, false);


        return mView;
    }
}