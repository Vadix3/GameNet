package com.vadim.gamenet

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration


class MyAppClass : Application() {

    object Constants {
        const val TAG = "myTag"
        public val app = App(
            AppConfiguration.Builder(BuildConfig.MONGODB_REALM_APP_ID)
                .defaultSyncErrorHandler { _, error ->
                    Log.e(TAG, "Sync error: ${error.errorMessage}")
                }
                .build())
    }

    companion object Functions {
        fun displayToast(myContext: Context, message: String) {
            Toast.makeText(myContext, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Realm.init(this) // context, usually an Activity or Application
        val appID: String = "gamenet-xopej" // replace this with your App ID "\"${gamenet-xopej}\""
        val app: App = App(AppConfiguration.Builder(appID).build())

    }
}