package com.vadim.gamenet.activities

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.adapters.ViewPagerAdapter
import com.vadim.gamenet.fragments.FragmentMainFeed
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.Conversation
import com.vadim.gamenet.models.FriendRequest
import com.vadim.gamenet.models.Game
import com.vadim.gamenet.utils.MongoTools
import io.realm.mongodb.App
import org.bson.Document
import org.json.JSONObject
import java.lang.reflect.Type


class MainActivity : AppCompatActivity() {

    private lateinit var app: App
    private lateinit var myUser: AppUser
    private lateinit var navigation: BottomNavigationView
    private lateinit var viewPager: ViewPager2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        app = MyAppClass.Constants.app
        val myUser = app.currentUser()
        if (myUser != null) {
            val customUserData: Document? = myUser.customData
            Log.v(TAG, "Fetched custom user data: $customUserData")
        }

        navigation = findViewById(R.id.main_bottom_navigation)
        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_menu_feed -> {
                    viewPager.currentItem = 0
                }
                R.id.bottom_menu_find -> {
                    viewPager.currentItem = 1
                }
                R.id.bottom_menu_chat -> {
                    viewPager.currentItem = 2
                }
                R.id.bottom_menu_profile -> {
                    viewPager.currentItem = 3
                }
            }
            true
        }
        viewPager = findViewById(R.id.main_LAY_pager)
        fetchUserDetails()
        initActionBar()
        initViewPager()
        loadMainFragment()
    }


    /**
     * A method to initialize the cool viewpager
     */
    private fun initViewPager() {
        Log.d(TAG, "initViewPager: ")
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle, myUser)
        viewPager.adapter = viewPagerAdapter
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> navigation.selectedItemId = R.id.bottom_menu_feed
                    1 -> navigation.selectedItemId = R.id.bottom_menu_find
                    2 -> navigation.selectedItemId = R.id.bottom_menu_chat
                    3 -> navigation.selectedItemId = R.id.bottom_menu_profile
                }
            }
        })
    }

    private fun fetchUserDetails() {
        Log.d(TAG, "fetchUserDetails: ")
        val userString = intent.getStringExtra("custom_data")
        val userJson = JSONObject(userString)
        val uId = userJson.get("user_id") as String
        val uName = userJson.get("username") as String
        val fName = userJson.get("first_name") as String
        val lName = userJson.get("last_name") as String
        val uEmail = userJson.get("email") as String
        val uCountry = userJson.get("country") as String
        val uGender = userJson.get("gender") as String
        val uPhotoUrl = userJson.get("photo_url") as String

        val spokenLangaugesType: Type = object : TypeToken<ArrayList<String>>() {}.type
        val listOfGamesType: Type = object : TypeToken<ArrayList<Game>>() {}.type
        val friendListType: Type = object : TypeToken<ArrayList<AppUser>>() {}.type
        val conversationListType: Type = object : TypeToken<ArrayList<Conversation>>() {}.type
        val requestListType: Type = object : TypeToken<ArrayList<FriendRequest>>() {}.type

        val spokenLanguages = Gson().fromJson(
            userJson.get(MongoTools.KEYS.SPOKEN_LANGUAGES) as String,
            spokenLangaugesType
        ) as ArrayList<String>
        val listOfGames = Gson().fromJson(
            userJson.get(MongoTools.KEYS.GAMES_LIST) as String,
            listOfGamesType
        ) as ArrayList<Game>
        val conversationList = Gson().fromJson(
            userJson.get(MongoTools.KEYS.LIST_OF_CONVERSATIONS) as String,
            conversationListType
        ) as ArrayList<Conversation>
        val friendList = Gson().fromJson(
            userJson.get(MongoTools.KEYS.FRIENDS_LIST) as String,
            friendListType
        ) as ArrayList<AppUser>
        val friendRequests = Gson().fromJson(
            userJson.get(MongoTools.KEYS.FRIEND_REQUESTS) as String,
            requestListType
        ) as ArrayList<FriendRequest>

        //TODO: Fetch conversation list
        myUser = AppUser(
            uId,
            uName,
            fName,
            lName,
            uEmail,
            "",
            uCountry,
            uGender,
            uPhotoUrl,
            spokenLanguages,
            listOfGames,
            friendList,
            conversationList,
            friendRequests
        )
        Log.d(TAG, "fetchUserDetails: user = $myUser")
    }

    private fun initActionBar() {
        Log.d(TAG, "initActionBar: ")
        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.colorPrimary)))
    }

    private fun loadMainFragment() {
        Log.d(TAG, "loadMainFragment: ")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_LAY_mainFrame, FragmentMainFeed(myUser))
        transaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bar_menu, menu);
        return true;
    }
}