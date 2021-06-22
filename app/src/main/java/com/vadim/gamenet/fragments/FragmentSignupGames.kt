package com.vadim.gamenet.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.adapters.GameListAdapter
import com.vadim.gamenet.interfaces.RegistrationCallback
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.Game
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class FragmentSignupGames(tempUser: AppUser) : Fragment() {

    //TODO: improve visualization, performance and edge cases

    private val tempUser: AppUser = tempUser
    private lateinit var searchBox: MaterialSearchBar
    private lateinit var gamesList: RecyclerView
    private lateinit var continueBtn: MaterialButton
    private val topResultsArray: ArrayList<Game> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MyAppClass.Constants.TAG, "onCreateView: ")
        val mView = inflater.inflate(R.layout.fragment_signup_games, container, false);

        initViews(mView)
        initSearchBar();
        return mView;
    }

    /** A method to initialize the game search bar*/
    private fun initSearchBar() {
        Log.d(TAG, "initSearchBar: ")

        searchBox.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener {
            override fun onSearchStateChanged(enabled: Boolean) {
                Log.d(TAG, "onSearchStateChanged: ")
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                Log.d(TAG, "onSearchConfirmed: ")
                val searchQuery = text.toString()
                searchBox.clearSuggestions()
                if (text == null) {
                } else {
                    Thread {
                        getResultsFromServer(searchQuery)
                    }.start()
                }
            }

            override fun onButtonClicked(buttonCode: Int) {
                Log.d(TAG, "onButtonClicked: ")

                when (buttonCode) {
                    MaterialSearchBar.BUTTON_NAVIGATION -> {
                        Log.d(
                            TAG,
                            "onButtonClicked: Button navigation"
                        )
                    }
                    MaterialSearchBar.BUTTON_BACK -> {
                        if (searchBox.isSuggestionsVisible)
                            searchBox.hideSuggestionsList()
                    }
                }
            }
        })


        //Text change
        searchBox.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d(TAG, "beforeTextChanged: ")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: Prediction request: " + s.toString())
                searchBox.clearSuggestions()
            }

            override fun afterTextChanged(s: Editable?) {
                Log.d(TAG, "afterTextChanged: ")

            }
        })

        searchBox.setSuggestionsClickListener(object : SuggestionsAdapter.OnItemViewClickListener {
            override fun OnItemClickListener(position: Int, v: View?) {
                val relevantGame = topResultsArray[position]
                Log.d(TAG, "OnItemClickListener: picked game: $relevantGame")
                searchBox.clearSuggestions()
                tempUser.listOfGames.add(relevantGame)
                populateRecycleView()
            }

            override fun OnItemDeleteListener(position: Int, v: View?) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun populateRecycleView() {
        Log.d(TAG, "populateRecycleView: ")
        val adapter = GameListAdapter(requireContext(), tempUser.listOfGames)
        gamesList.adapter = adapter
    }

    /** A method to get game query results from the server*/
    private fun getResultsFromServer(searchQuery: String) {
        Log.d(TAG, "getResultsFromServer: Searching: $searchQuery")

        val url =
            "https://www.giantbomb.com/api/search/" +
                    "?api_key=${resources.getString(R.string.giant_bomb_api)}" +
                    "&query=$searchQuery" +
                    "&resources=game" +
                    "&format=json" +
                    "&number_of_page_results=5"

        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .get()
            .build()

        val call: Call = okHttpClient.newCall(request)

        topResultsArray.clear()

        try {

            val response = call.execute()
            Log.d(TAG, "getResultsFromServer: Response code = ${response.code}")

            val responseBody: String? = response.body?.string()
            Log.d(TAG, "fetchBuildingsFromServer: $responseBody")

            val totalJsonObject = JSONObject(responseBody)
            val gameJsonArray = totalJsonObject.get("results") as JSONArray

            var numOfResults = 5;
            if (gameJsonArray.length() < numOfResults) {
                numOfResults = gameJsonArray.length()
            }

            for (i in 0 until numOfResults) {
                val tempJson = gameJsonArray[i] as JSONObject
                val imageResults = tempJson.get("image") as JSONObject

                //maybe not all images always exist
                val imageUrl = imageResults.get("original_url") as String

                val name = tempJson.get("name") as String
                val id = tempJson.get("id") as Int
                val tempGame = Game(id, name, imageUrl)

                //TODO: Take care of duplicates
                topResultsArray.add(tempGame)
            }
            topResultsArray.reverse()
            populatePredictionList(topResultsArray)

        } catch (e: IOException) {
            Log.d(TAG, "postInfoToDb: " + e.message)
            e.printStackTrace()
        }
    }

    /** A method to populate the prediction list of the searchBox*/
    private fun populatePredictionList(topResultsArray: ArrayList<Game>) {
        Log.d(TAG, "populatePredictionList: Got list $topResultsArray")
        val gamesNames: ArrayList<String> = arrayListOf()
        for (e in topResultsArray) {
            gamesNames.add(e.name)
        }
        requireActivity().runOnUiThread {
            searchBox.clearSuggestions()
            searchBox.updateLastSuggestions(gamesNames)
        }
    }

    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: ")
        searchBox = mView.findViewById(R.id.signupGames_LAY_searchBar)
        gamesList = mView.findViewById(R.id.signupGames_LST_gamesList)
        continueBtn = mView.findViewById(R.id.signupGames_BTN_continue)
        continueBtn.setOnClickListener {
            goToLanguagesList()
        }
    }

    private fun goToLanguagesList() {
        Log.d(TAG, "goToLanguagesList: ")
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        )
        transaction.replace(R.id.login_signup_frame, FragmentSignupLanguages(requireContext() as RegistrationCallback,tempUser))
        transaction.addToBackStack("games_transaction")
        transaction.commit()
    }
}