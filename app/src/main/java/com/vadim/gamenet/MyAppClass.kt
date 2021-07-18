package com.vadim.gamenet

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration


class MyAppClass : Application() {


    object Constants {
        const val TAG = "myTag"
        const val STORAGE_PERMISSION_CODE = 0
        const val CAMERA_PERMISSION_CODE = 1

        val app = App(
            AppConfiguration.Builder(BuildConfig.MONGODB_REALM_APP_ID)
                .defaultSyncErrorHandler { _, error ->
                    Log.e(Constants.TAG, "Sync error: ${error.errorMessage}")
                }
                .build())
    }


    companion object Functions {
        fun displayToast(myContext: Context, message: kotlin.String) {
            Toast.makeText(myContext, message, Toast.LENGTH_LONG).show()
        }

        fun openStorage(resultLauncher: ActivityResultLauncher<Intent>) {
            Log.d(Constants.TAG, "openStorage: ")

            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            resultLauncher.launch(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Realm.init(this) // context, usually an Activity or Application
        val appID: kotlin.String = "gamenet-xopej" // replace this with your App ID "\"${gamenet-xopej}\""
        val app: App = App(AppConfiguration.Builder(appID).build())

    }
}