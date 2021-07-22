package com.vadim.gamenet.activities

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.adapters.ViewPagerAdapter
import com.vadim.gamenet.dialogs.DialogUserDetails
import com.vadim.gamenet.fragments.FragmentConversation
import com.vadim.gamenet.fragments.FragmentMainFeed
import com.vadim.gamenet.interfaces.OpenChatCallback
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.Game
import com.vadim.gamenet.utils.MongoTools
import com.vadim.gamenet.utils.ParsingTools
import io.realm.mongodb.App
import org.bson.Document
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type


class MainActivity : AppCompatActivity(), OpenChatCallback {

    private lateinit var app: App
    private lateinit var myUser: AppUser
    private lateinit var navigation: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var main_TLB_head: Toolbar
    private lateinit var main_SRC_search: MaterialSearchView
    private lateinit var myMenuItem : MenuItem
    private val context: Context = this
    private val suggestedUsers = arrayListOf<AppUser>()
    private val suggestedNames = arrayListOf<kotlin.String>()
    private var isChatWindow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_TLB_head = findViewById(R.id.main_TLB_head)
        main_SRC_search = findViewById(R.id.main_SRC_search)
        main_SRC_search.setBackgroundColor(getColor(R.color.white))
        main_SRC_search.showSuggestions();

        app = MyAppClass.Constants.app
        val myUser = app.currentUser()
        if (myUser != null) {
            val customUserData: Document? = myUser.customData
            Log.v(TAG, "Fetched custom user data: $customUserData")
        }

        navigation = findViewById(R.id.main_bottom_navigation)
        viewPager = findViewById(R.id.main_LAY_pager)
        fetchUserDetails()
        initViewPager()
        loadMainFragment()
        searchAction()
        initActionBar()
    }

    private fun initNavigationListener() {
        Log.d(TAG, "initNavigationListener: ")
        myMenuItem.isVisible = false
        navigation.setOnNavigationItemSelectedListener { item ->
            if (main_SRC_search.isSearchOpen) {
                main_SRC_search.closeSearch();
            }
            when (item.itemId) {
                R.id.bottom_menu_feed -> {
                    viewPager.currentItem = 0
                    main_TLB_head.title = getString(R.string.main_feed)
                    myMenuItem.isVisible = false
                }
                R.id.bottom_menu_find -> {
                    viewPager.currentItem = 1
                    main_TLB_head.title = getString(R.string.friends)
                    myMenuItem.isVisible = true
                }
                R.id.bottom_menu_chat -> {
                    viewPager.currentItem = 2
                    main_TLB_head.title = getString(R.string.chat)
                    myMenuItem.isVisible = false
                }
                R.id.bottom_menu_profile -> {
                    viewPager.currentItem = 3
                    main_TLB_head.title = getString(R.string.profile)
                    myMenuItem.isVisible = false
                }
            }
            true
        }
    }

    /**
     * A method to initialize the cool viewpager
     */
    private fun initViewPager() {
        Log.d(TAG, "initViewPager: ")
        val viewPagerAdapter = ViewPagerAdapter(this, supportFragmentManager, lifecycle, myUser)
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
        val uId = userJson.get("user_id") as kotlin.String
        val uName = userJson.get("username") as kotlin.String
        val fName = userJson.get("first_name") as kotlin.String
        val lName = userJson.get("last_name") as kotlin.String
        val uEmail = userJson.get("email") as kotlin.String
        val uCountry = userJson.get("country") as kotlin.String
        val uGender = userJson.get("gender") as kotlin.String
        val uPhotoUrl = userJson.get("photo_url") as kotlin.String

        val spokenLangaugesType: Type = object : TypeToken<ArrayList<kotlin.String>>() {}.type
        val listOfGamesType: Type = object : TypeToken<ArrayList<Game>>() {}.type
        val stringType: Type = object : TypeToken<ArrayList<kotlin.String>>() {}.type
        val conversationListType: Type = object : TypeToken<ArrayList<String>>() {}.type
//        val requestListType: Type = object : TypeToken<ArrayList<FriendRequest>>() {}.type

        val spokenLanguages = Gson().fromJson(
            userJson.get(MongoTools.USER_KEYS.SPOKEN_LANGUAGES) as kotlin.String,
            spokenLangaugesType
        ) as ArrayList<kotlin.String>
        val listOfGames = Gson().fromJson(
            userJson.get(MongoTools.USER_KEYS.GAMES_LIST) as kotlin.String,
            listOfGamesType
        ) as ArrayList<Game>
        val conversationList = Gson().fromJson(
            userJson.get(MongoTools.USER_KEYS.LIST_OF_CONVERSATIONS) as kotlin.String,
            conversationListType
        ) as ArrayList<String>
        val friendList = Gson().fromJson(
            userJson.get(MongoTools.USER_KEYS.FRIENDS_LIST) as kotlin.String,
            stringType
        ) as ArrayList<kotlin.String>
//        val friendRequests = Gson().fromJson(
//            userJson.get(MongoTools.KEYS.FRIEND_REQUESTS) as String,
//            requestListType
//        ) as ArrayList<FriendRequest>

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
        )
        Log.d(TAG, "fetchUserDetails: user = $myUser")
    }

    /** A method to define what happenes when you search for something*/
    private fun searchAction() {
        Log.d(TAG, "searchAction: ")
        main_SRC_search.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: kotlin.String?): Boolean {
                Log.d(TAG, "onQueryTextSubmit: $query")
                suggestedUsers.clear()
                suggestedNames.clear()
                main_SRC_search.dismissSuggestions()
                val user = app.currentUser()
                if (user != null) {
                    val tools = MongoTools(context, user, object : MongoTools.ResultListener {
                        override fun getResult(result: Boolean, message: kotlin.String) {
                            if (result) {
                                Log.d(TAG, "getResult: got suggestions: $message")
                                val array = JSONArray(message)
                                if (array.length() == 0) {
                                    Log.d(TAG, "getResult: empty results")
                                    suggestedUsers.clear()
                                    suggestedNames.clear()
                                    main_SRC_search.dismissSuggestions()
                                } else {
                                    for (i in 0 until array.length()) {
                                        val tempUser = ParsingTools.parseUser(array[i].toString())
                                        if (tempUser.email != myUser.email) {
                                            suggestedUsers.add(tempUser)
                                            suggestedNames.add(tempUser.first_name + " " + tempUser.last_name)
                                        }
                                    }
                                }
                                refreshSearchList(suggestedUsers, suggestedNames)
                            } else {
                                Log.d(TAG, "getResult: Error: $message")
                            }
                        }

                    })


                    val query1 = Document("\$regex", "^$query.*")
                    query1["\$options"] = "i"
                    val query2 = Document(MongoTools.USER_KEYS.FIRST_NAME, query1)

                    Log.d(TAG, "onQueryTextSubmit: final query: $query2")

                    tools?.fetchDocumentFromDatabase(user, "gamenet_users", "custom_data", query2)

                }
                return false
            }

            override fun onQueryTextChange(newText: kotlin.String?): Boolean { // when text is changed
                Log.d(TAG, "onQueryTextChange: $newText")
                suggestedUsers.clear()
                suggestedNames.clear()
                main_SRC_search.dismissSuggestions()
                val user = app.currentUser()
                if (user != null) {
                    val tools = MongoTools(context, user, object : MongoTools.ResultListener {
                        override fun getResult(result: Boolean, message: kotlin.String) {
                            if (result) {
                                Log.d(TAG, "getResult: got suggestions: $message")
                                val array = JSONArray(message)
                                if (array.length() == 0) {
                                    Log.d(TAG, "getResult: no suggestions")
                                    suggestedUsers.clear()
                                    suggestedNames.clear()
                                    main_SRC_search.dismissSuggestions()
                                } else {
                                    for (i in 0 until array.length()) {
                                        val tempUser = ParsingTools.parseUser(array[i].toString())
                                        if (tempUser.email != myUser.email) {
                                            suggestedUsers.add(tempUser)
                                            suggestedNames.add(tempUser.first_name + " " + tempUser.last_name)
                                        }
                                    }
                                }
                                refreshSearchList(suggestedUsers, suggestedNames)
                            } else {
                                Log.d(TAG, "getResult: Error: $message")
                            }
                        }

                    })


                    val query1 = Document("\$regex", "^$newText.*")
                    query1["\$options"] = "i"
                    val query2 = Document(MongoTools.USER_KEYS.FIRST_NAME, query1)

                    Log.d(TAG, "onQueryTextSubmit: final query: $query2")

                    tools?.fetchDocumentFromDatabase(user, "gamenet_users", "custom_data", query2)

                }
                return false
            }
        })

        main_SRC_search.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewShown() {
                Log.d(TAG, "onSearchViewShown: ")
            }

            override fun onSearchViewClosed() {
                Log.d(TAG, "onSearchViewClosed: ")
            }

        })


    }

    private fun refreshSearchList(
        suggestedUsers: ArrayList<AppUser>,
        suggestedNames: ArrayList<String>
    ) {
        Log.d(TAG, "refreshSearchList: $suggestedNames $suggestedUsers")
        if (suggestedUsers.size == 0) {
            main_SRC_search.dismissSuggestions()
        } else {
            val namesArr: Array<kotlin.String> =
                suggestedNames.toArray(arrayOfNulls<kotlin.String>(suggestedNames.size))
            main_SRC_search.setSuggestions(namesArr)
            main_SRC_search.setOnItemClickListener { _, _, position, _ ->
                Log.d(TAG, "onItemClick: $position")
                openUserDetailsDialog(suggestedUsers[position])
            }
            main_SRC_search.showSuggestions()
        }
    }

    private fun openUserDetailsDialog(temp: AppUser) {
        Log.d(TAG, "openUserDetailsDialog: $temp")
        val dialog = DialogUserDetails(context, temp, myUser, DialogUserDetails.MODE.FRIEND_LIST)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val width = (context.resources.displayMetrics.widthPixels * 0.8).toInt()
        dialog.window!!.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setDimAmount(0.9f)
    }

    private fun initActionBar() {
        Log.d(TAG, "initActionBar: ")
        main_SRC_search.setBackgroundColor(getColor(R.color.white))
        main_SRC_search.showSuggestions()
        setSupportActionBar(main_TLB_head)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.colorPrimary)))
    }

    private fun loadMainFragment() {
        Log.d(TAG, "loadMainFragment: ")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_LAY_mainFrame, FragmentMainFeed(myUser))
        transaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            menuInflater.inflate(R.menu.search_menu, menu)
            myMenuItem = menu.findItem(R.id.action_search)
            initNavigationListener()
            main_SRC_search.setMenuItem(myMenuItem)
        }
        return super.onCreateOptionsMenu(menu)
    }


    override fun onBackPressed() {
        if (isChatWindow) {
            viewPager.visibility = View.VISIBLE
            navigation.visibility = View.VISIBLE
            viewPager.currentItem = 2
            main_TLB_head.title = getString(R.string.chat)
        }
        if (main_SRC_search.isSearchOpen) {
            main_SRC_search.closeSearch()
        } else {
            super.onBackPressed();
        }
    }

    override fun getFragment(fragment: FragmentConversation, name: String) {
        Log.d(TAG, "getFragment: ")
        viewPager.visibility = View.GONE
        navigation.visibility = View.GONE
        isChatWindow = true
        main_TLB_head.title = name

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        )
        transaction.replace(R.id.main_LAY_mainFrame, fragment)
        transaction.addToBackStack("chat_transaction")
        transaction.commit()
    }
}