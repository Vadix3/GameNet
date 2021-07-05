package com.vadim.gamenet.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.fragments.FragmentLogin
import com.vadim.gamenet.fragments.FragmentSignupUsername
import com.vadim.gamenet.interfaces.RegistrationCallback
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.Game
import com.vadim.gamenet.utils.MongoTools
import io.realm.mongodb.App
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.mongo.MongoClient
import io.realm.mongodb.mongo.MongoCollection
import io.realm.mongodb.mongo.MongoDatabase
import org.bson.Document


class LoginSignupActivity : AppCompatActivity(), RegistrationCallback {

    private lateinit var app: App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        app = MyAppClass.Constants.app
        supportActionBar?.hide()
        startSequence()
    }

    private fun startSequence() {
        Log.d(TAG, "startSequence: ")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.login_signup_frame, FragmentLogin(app, null))
        transaction.commit()
    }

    override fun getNewUser(tempUser: AppUser) {
        Log.d(TAG, "getNewUser: $tempUser")
        saveUserToServer(tempUser)
    }

    /** A method to save the user to the server*/
    private fun saveUserToServer(tempUser: AppUser) {
        Log.d(TAG, "saveUserToServer: ")
        app.emailPassword.registerUserAsync(tempUser.email, tempUser.password) {
            if (!it.isSuccess) {
                Log.e(TAG, "Error: ${it.error}")
                //TODO: Handle user not saved
            } else {
                Log.i(TAG, "Successfully registered user.")
                Toast.makeText(this, "User created successfully!", Toast.LENGTH_SHORT).show()
                loginUserToSaveData(app, tempUser) // login to save the custom data
                supportFragmentManager.beginTransaction().setCustomAnimations(
                    R.anim.enter_from_right,
                    R.anim.exit_to_left,
                    R.anim.enter_from_left,
                    R.anim.exit_to_right
                )
                    .replace(R.id.login_signup_frame, FragmentLogin(app, tempUser))
                    .commit()
            }
        }
    }

    private fun loginUserToSaveData(app: App, tempUser: AppUser) {
        Log.d(TAG, "loginUser: ")
        val cred: Credentials = Credentials.emailPassword(tempUser.email, tempUser.password)
        app.loginAsync(cred) {
            if (it.isSuccess) {
                val user = app.currentUser()
                if (user != null) {

                    val tools = MongoTools(this, user, object : MongoTools.ResultListener {
                        override fun getResult(result: Boolean, message: String) {
                            if (result) {
                                Log.d(TAG, "getResult: SUCCESS: $message")

                            } else {
                                Log.d(TAG, "getResult: FAILURE: $message")
                            }
                        }

                        override fun getQueriedUsers(
                            result: Boolean,
                            message: String,
                            userList: ArrayList<AppUser>
                        ) {
                            Log.d(TAG, "getResult: SUCCESS: $message")
                        }
                    })
                    tools.saveUserToDatabase(user, tempUser)
                }
            } else {
                Log.e(TAG, "Failed to log in anonymously: ${it.error}")
                //TODO: Handle login problems
            }
        }
    }

}