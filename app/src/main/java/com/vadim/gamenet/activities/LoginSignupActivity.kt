package com.vadim.gamenet.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.vadim.gamenet.BuildConfig
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.fragments.FragmentLogin
import com.vadim.gamenet.interfaces.RegistrationCallback
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.Game
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
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

        app = App(
            AppConfiguration.Builder(BuildConfig.MONGODB_REALM_APP_ID)
                .defaultSyncErrorHandler { _, error ->
                    Log.e(TAG, "Sync error: ${error.errorMessage}")
                }
                .build())

        supportActionBar?.hide()
        startSequence()
    }

    private fun createDemoUser(): AppUser {

        val game1 = Game(
            25249,
            "Red Dead Redemption",
            "https://www.giantbomb.com/a/uploads/original/8/82063/2737123-reddeadredemption.jpg"
        )
        val game2 = Game(
            36765,
            "Grand Theft Auto V",
            "https://www.giantbomb.com/a/uploads/original/0/3699/2463980-grand%20theft%20auto%20v.jpg"
        )
        val game3 = Game(
            78229,
            "Assassin's Creed Valhalla",
            "https://www.giantbomb.com/a/uploads/original/45/459166/3251329-creed.png"
        )

        val langList = arrayListOf("English", "Hebrew", "Russian")
        val gameList = arrayListOf(game1, game2, game3)
        val friendsList = arrayListOf<AppUser>()
        return AppUser(
            "",
            "Vadix3",
            "Vadim",
            "Kandaurov",
            "vadix3@gmail.com",
            "Vx121212",
            "Israel",
            langList,
            "Male",
            gameList,
            friendsList
        )
    }

    private fun startSequence() {
        Log.d(TAG, "startSequence: ")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.login_signup_frame, FragmentLogin(app, null))
        transaction.addToBackStack("signup_transaction")
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
                    saveUserCustomData(user, tempUser)
                } else {
                    Log.d(TAG, "loginUser: ")
                }
            } else {
                Log.e(TAG, "Failed to log in anonymously: ${it.error}")
                //TODO: Handle login problems
            }
        }
    }

    private fun saveUserCustomData(user: User, tempUser: AppUser) {
        Log.d(TAG, "saveUserCustomData: ")
        val mongoClient: MongoClient =
            user.getMongoClient("mongodb-atlas")!! // service for MongoDB Atlas cluster containing custom user data
        val mongoDatabase: MongoDatabase =
            mongoClient.getDatabase("gamenet_users")!!
        val mongoCollection: MongoCollection<Document> =
            mongoDatabase.getCollection("custom_data")!!
        Log.d(TAG, "saveUserToServer: userID = ${user.id}")
        mongoCollection.insertOne(
            Document("user_id", user.id)
                .append("username", tempUser.userName)
                .append("first_name", tempUser.firstName)
                .append("last_name", tempUser.lastName)
                .append("email", tempUser.email)
                .append("country", tempUser.country)
                .append("gender", tempUser.gender)
                .append("spoken_languages", Gson().toJson(tempUser.spokenLanguages))
                .append("games_list", Gson().toJson(tempUser.listOfGames))
                .append("friends_list", Gson().toJson(tempUser.friend_list))
        )
            .getAsync { result ->
                if (result.isSuccess) {
                    Log.v(
                        TAG,
                        "Inserted custom user data document. _id of inserted document: ${result.get().insertedId}"
                    )
                } else {
                    Log.e(
                        TAG,
                        "Unable to insert custom user data. Error: ${result.error}"
                    )
                    //TODO:Handle problems with details
                }
            }
    }
}