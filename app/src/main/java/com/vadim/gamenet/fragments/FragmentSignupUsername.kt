package com.vadim.gamenet.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.models.AppUser

class FragmentSignupUsername(tempUser: AppUser) : Fragment() {

    private val tempUser: AppUser = tempUser
    private lateinit var usernameInputBox: TextInputLayout
    private lateinit var usernameInputEdt: TextInputEditText
    private lateinit var continueBtn: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(MyAppClass.Constants.TAG, "onCreateView: ")
        val mView = inflater.inflate(R.layout.fragment_signup_username, container, false);
        initViews(mView)

        return mView;
    }


    private fun initViews(mView: View) {
        Log.d(TAG, "initViews newUsername: ")
        usernameInputBox = mView.findViewById(R.id.newAccountUsername_LAY_userNameLayout)
        usernameInputEdt = mView.findViewById(R.id.newAccountUsername_EDT_username)
        usernameInputEdt.addTextChangedListener { usernameInputBox.error = null }
        continueBtn = mView.findViewById(R.id.newAccountUsername_BTN_continue)
        continueBtn.setOnClickListener {
            checkValidUsername()
        }
    }

    private fun checkValidUsername() {
        Log.d(TAG, "checkValidUsername: ")
        if (usernameInputEdt.text.toString().trim().isEmpty()) { // Check last name not empty
            Log.d(TAG, "checkValidInput: No input for userName")
            val problem = "Please enter userName"
            usernameInputBox.error = problem
        } else {
            usernameInputBox.error = null
            tempUser.userName = usernameInputEdt.text.toString()
            moveToGamesFragment()
        }
    }

    private fun moveToGamesFragment() {
        Log.d(TAG, "moveToGamesFragment: ")
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        )
        transaction.replace(R.id.login_signup_frame, FragmentSignupGames(tempUser))
        transaction.addToBackStack("games_transaction")
        transaction.commit()
    }
}