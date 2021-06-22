package com.vadim.gamenet.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.adapters.LanguageSelectionAdapter
import com.vadim.gamenet.interfaces.RegistrationCallback
import com.vadim.gamenet.models.AppUser
import org.json.JSONObject
import java.io.*


class FragmentSignupLanguages(callback: RegistrationCallback, tempUser: AppUser) : Fragment() {

    private val tempUser: AppUser = tempUser
    private val callback:RegistrationCallback=callback
    private lateinit var plainLanguages: JSONObject
    private lateinit var languagesLayout: RecyclerView
    private lateinit var continueBtn: MaterialButton
    private lateinit var languageSpinner: Spinner
    private var selectionCount = 0;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        val mView = inflater.inflate(R.layout.fragment_signup_languages, container, false)
        initViews(mView)
        return mView;
    }

    private fun initViews(mView: View) {
        Log.d(TAG, "initViews: ")
        languagesLayout = mView.findViewById(R.id.signupLanguages_LST_languagesRecycler)
        continueBtn = mView.findViewById(R.id.signupLanguages_BTN_continue)
        continueBtn.setOnClickListener {
            if (tempUser.spokenLanguages.size == 0) {
                Toast.makeText(requireContext(), "Please select a language", Toast.LENGTH_SHORT)
                    .show()
            } else {
                finishRegistration()
            }
        }
        languageSpinner = mView.findViewById(R.id.signupLanguages_LAY_genderSpinner)
        readListFromResources()
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (selectionCount > 0) {
                    val langArray = resources.getStringArray(R.array.languages_all)
                    val key = langArray[position]
                    Log.d(TAG, "onItemSelected: selected: $key")
                    Log.d(TAG, "onItemSelected: code: " + plainLanguages.get(key))
                    tempUser.spokenLanguages.add(key)
                    populateLanguageRecycler()

                }
                selectionCount++;
            }

        }
    }

    private fun finishRegistration() {
        Log.d(TAG, "finishRegistration: ")
        callback.getNewUser(tempUser)
    }

    private fun populateLanguageRecycler() {
        Log.d(TAG, "populateRecycleView: ")
        val isoList: ArrayList<String> = arrayListOf()
        for (item in tempUser.spokenLanguages) {
            isoList.add(plainLanguages.get(item).toString())
        }
        val adapter = LanguageSelectionAdapter(requireContext(), isoList)
        languagesLayout.adapter = adapter
    }

    private fun readListFromResources() {
        Log.d(TAG, "readListFromResources: ")
        val raw = resources.openRawResource(R.raw.language_iso)
        val writer: Writer = StringWriter()
        val buffer = CharArray(1024)
        raw.use { rawData ->
            val reader: Reader = BufferedReader(InputStreamReader(rawData, "UTF-8"))
            var n: Int
            while (reader.read(buffer).also { n = it } != -1) {
                writer.write(buffer, 0, n)
            }
        }
        plainLanguages = JSONObject(writer.toString())
    }

}