package com.vadim.gamenet.activities

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.fragments.FragmentMainFeed

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initActionBar();
        loadMainFragment()
    }

    private fun initActionBar() {
        Log.d(TAG, "initActionBar: ")
        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.colorPrimary)))
    }

    private fun loadMainFragment() {
        Log.d(TAG, "loadMainFragment: ")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_LAY_mainFrame, FragmentMainFeed())
        transaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bar_menu, menu);
        return true;
    }

}