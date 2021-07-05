package com.vadim.gamenet.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.models.AppUser

//TODO: Add each input out of focus check
//TODO: Check input gui errors

class FragmentSignup : Fragment() {
    private lateinit var firstNameBox: TextInputEditText
    private lateinit var lastNameBox: TextInputEditText
    private lateinit var emailBox: TextInputEditText
    private lateinit var passwordBox: TextInputEditText
    private lateinit var confirmPasswordBox: TextInputEditText
    private lateinit var genderList: Spinner
    private lateinit var countryList: Spinner
    private lateinit var submitBtn: MaterialButton

    private lateinit var firstNameLayout: TextInputLayout
    private lateinit var lastNameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        val mView = inflater.inflate(R.layout.fragment_signup, container, false)
        findViews(mView)
        initFocusChangeListeners()
        return mView;
    }

    /** A method to enable the focus change listeners to remove errors*/
    private fun initFocusChangeListeners() {
        Log.d(TAG, "initFocusChangeListeners: ")

        firstNameBox.addTextChangedListener { firstNameLayout.error = null }
        lastNameBox.addTextChangedListener { lastNameLayout.error = null }
        emailBox.addTextChangedListener { emailLayout.error = null }
        passwordBox.addTextChangedListener { passwordLayout.error = null }
        confirmPasswordBox.addTextChangedListener { confirmPasswordLayout.error = null }
    }

    /** A method to find the views*/
    private fun findViews(mView: View) {
        Log.d(TAG, "findViews: ")
        firstNameLayout = mView.findViewById(R.id.newAccount_LAY_firstNameLayout)
        lastNameLayout = mView.findViewById(R.id.newAccount_LAY_lastNameLayout)
        emailLayout = mView.findViewById(R.id.newAccount_LAY_emailLayout)
        passwordLayout = mView.findViewById(R.id.newAccount_LAY_passwordLayout)
        confirmPasswordLayout = mView.findViewById(R.id.newAccount_LAY_confirmPasswordLayout)
        firstNameBox = mView.findViewById(R.id.newAccount_EDT_firstName)
        lastNameBox = mView.findViewById(R.id.newAccount_EDT_lastName)
        emailBox = mView.findViewById(R.id.newAccount_EDT_email)
        passwordBox = mView.findViewById(R.id.newAccount_EDT_password)
        confirmPasswordBox = mView.findViewById(R.id.newAccount_EDT_confirmPassword)
        genderList = mView.findViewById(R.id.newAccount_LAY_genderSpinner)
        countryList = mView.findViewById(R.id.newAccount_LAY_countrySpinner)
        submitBtn = mView.findViewById(R.id.newAccount_BTN_submit)
        submitBtn.setOnClickListener {
            val checkResult = checkValidInput() // get a result from the input check
            if (checkResult == "All good!") {
                Log.d(TAG, "check: All good!")
                moveToUsernameSelection()
            } else {
                Log.d(TAG, "check: $checkResult")
            }
        }
    }

    /** A method to move to the next signup section*/
    private fun moveToUsernameSelection() {
        Log.d(TAG, "moveToUsernameSelection: ")
        val tempUser = AppUser()
        tempUser.first_name = firstNameBox.text.toString()
        tempUser.last_name = lastNameBox.text.toString()
        tempUser.email = emailBox.text.toString().toLowerCase()
        tempUser.password = passwordBox.text.toString()
        tempUser.gender = genderList.selectedItem.toString()
        tempUser.country = countryList.selectedItem.toString()

        Log.d(TAG, "moveToUsernameSelection: $tempUser")

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        )
        transaction.replace(R.id.login_signup_frame, FragmentSignupUsername(tempUser))
//        transaction.addToBackStack("username_transaction")
        transaction.commit()

    }

    /** A method that will check for valid input in the input boxes*/
    private fun checkValidInput(): String {
        Log.d(TAG, "checkValidInput: ")
        /** A check will be performed on each box and will return false if there is a problem.
         * if there is none, it will return true
         */
        if (firstNameBox.text.toString().trim().isEmpty()) { // Check first name not empty
            Log.d(TAG, "checkValidInput: No input for first name")
            val problem = "Please enter first name"
            firstNameLayout.error = problem
            return problem;
        } else {
            firstNameLayout.error = null
        }
        if (lastNameBox.text.toString().trim().isEmpty()) { // Check last name not empty
            Log.d(TAG, "checkValidInput: No input for last name")
            val problem = "Please enter last name"
            lastNameLayout.error = problem
            return problem;
        } else {
            lastNameLayout.error = null
        }
        if (emailBox.text.toString().trim().isEmpty()) { // Check email not empty
            Log.d(TAG, "checkValidInput: No input for email")
            val problem = "Please enter email"
            emailLayout.error = problem
            return problem;
        } else {
            emailLayout.error = null
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailBox.text.toString()).matches()) {
            // Check email format
            Log.d(TAG, "checkValidInput: bad email format")
            val problem = "Please enter proper email"
            emailLayout.error = problem
            return problem;
        } else {
            emailLayout.error = null
        }
        if (passwordBox.text.toString().trim().isEmpty()) { // Check password not empty
            Log.d(TAG, "checkValidInput: No input for password")
            val problem = "Please enter password"
            passwordLayout.error = problem
            return problem;
        } else {
            passwordLayout.error = null
        }
        if (passwordBox.text.toString().length < 6) { // Check password length
            Log.d(TAG, "checkValidInput: Short password") // TODO: password complexity check
            val problem = "Password too short"
            passwordLayout.error = problem
            return problem;
        } else {
            passwordLayout.error = null
        }
        if (confirmPasswordBox.text.toString() != passwordBox.text.toString()) { // Confirm password mismatch
            Log.d(TAG, "checkValidInput: Confirm password mismatch")
            val problem = "Passwords did not match"
            confirmPasswordLayout.error = problem
            return problem;
        } else {
            confirmPasswordLayout.error = null
        }
        if (genderList.selectedItem.toString() == "Gender") { // TODO: Problem with multi language here
            Log.d(TAG, "checkValidInput: No selected gender") // check gender selection
            Toast.makeText(requireContext(), "Please select a Gender", Toast.LENGTH_SHORT).show()
            return "Please select gender"
        }
        if (countryList.selectedItem.toString() == "Country") { // TODO: Problem with multi language here
            Log.d(TAG, "checkValidInput: No selected country") // check country selection
            Toast.makeText(requireContext(), "Please select a Country", Toast.LENGTH_SHORT).show()
            return "Please select country"
        }
        return "All good!";
    }

}