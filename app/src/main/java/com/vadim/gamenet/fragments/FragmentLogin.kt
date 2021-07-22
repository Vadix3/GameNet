package com.vadim.gamenet.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.activities.MainActivity
import com.vadim.gamenet.models.AppUser
import io.realm.mongodb.App
import io.realm.mongodb.Credentials
import io.realm.mongodb.User

class FragmentLogin(app: App?, tempUser: AppUser?) : Fragment() {
    private lateinit var signupButton: MaterialButton
    private lateinit var loginButton: MaterialButton
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var emailEdt: TextInputEditText
    private lateinit var passwordEdt: TextInputEditText
    private val tempUser = tempUser
    private val app = app

    @ExperimentalStdlibApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: fragmentLogin ")
        val mView = inflater.inflate(R.layout.fragment_login, container, false);
        initViews(mView)
//        loginApp("vadix3@gmail.com", "Vx121212") //TODO: remove after testing
        return mView
    }

    @ExperimentalStdlibApi
    private fun initViews(mView: View) {
        signupButton = mView.findViewById(R.id.login_BTN_signup)
        loginButton = mView.findViewById(R.id.login_BTN_login)
        signupButton.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.login_signup_frame, FragmentSignup())
            transaction.addToBackStack("signup_transaction")
            transaction.commit()
        }
        loginButton.setOnClickListener {
            checkUserInput()
        }

        emailLayout = mView.findViewById(R.id.login_LAY_emailLayout)
        passwordLayout = mView.findViewById(R.id.login_LAY_passwordLayout)

        emailEdt = mView.findViewById(R.id.login_Edt_emailEdt)
        emailEdt.addTextChangedListener { emailLayout.error = null }
        passwordEdt = mView.findViewById(R.id.login_Edt_passwordEdt)
        passwordEdt.addTextChangedListener { passwordLayout.error = null }

        if (tempUser != null) {
            emailEdt.setText(tempUser.email)
            passwordEdt.setText(tempUser.password)
        }
    }

    @ExperimentalStdlibApi
    private fun checkUserInput() {
        Log.d(TAG, "checkUserInput: ")
        var flag = false
        if (emailEdt.text.toString().trim().isEmpty()) { // Check email not empty
            Log.d(TAG, "checkValidInput: No input for email")
            val problem = "Please enter email"
            emailLayout.error = problem
            flag = true
        } else {
            emailLayout.error = null
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEdt.text.toString()).matches()) {
            // Check email format
            Log.d(TAG, "checkValidInput: bad email format")
            val problem = "Please enter a proper email address"
            emailLayout.error = problem
            flag = true
        } else {
            emailLayout.error = null
        }
        if (passwordEdt.text.toString().trim().isEmpty()) { // Check password not empty
            Log.d(TAG, "checkValidInput: No input for password")
            val problem = "Please enter password"
            passwordLayout.error = problem
            flag = true
        } else {
            passwordLayout.error = null
        }
        if (!flag) {
            Log.d(TAG, "checkUserInput: No problems!")
            loginApp(emailEdt.text.toString().lowercase(), passwordEdt.text.toString())
        }
    }

    private fun loginApp(email: String, password: String) {
        Log.d(TAG, "loginApp with: $email")
        val cred: Credentials = Credentials.emailPassword(email, password)
        app?.loginAsync(cred) {
            if (it.isSuccess) {
                val user = app.currentUser()
                if (user != null) {
                    requireActivity().runOnUiThread {
                        moveToMainActivity(user)
                    }
                } else {
                    Log.d(TAG, "loginUser: ")
                }
            } else {
                val error = it.error.errorCode.ordinal
                Log.e(TAG, "Failed to log in anonymously: $error")
                if (error == 174) {
                    Log.d(TAG, "loginApp: Email/Password problem")
                    Toast.makeText(
                        requireContext(),
                        "Email/Password problem",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun moveToMainActivity(user: User) {
        Log.d(TAG, "moveToMainActivity: ")
        val userCustomData = user.customData.toJson().toString()
        Log.d(TAG, "moveToMainActivity: custom data = $userCustomData")
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.putExtra("custom_data", userCustomData)
        startActivity(intent)
        requireActivity().finish()
    }


}