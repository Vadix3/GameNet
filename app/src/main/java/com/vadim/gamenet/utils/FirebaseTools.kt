package com.vadim.gamenet.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R

class FirebaseTools(context: Context,resultListener:UploadResultListener) {
    private val context = context
    private lateinit var storage: FirebaseStorage
    private val resultListener = resultListener

    interface UploadResultListener {
        fun getUploadResult(result: Boolean, imageUrl: String?)
        fun getDownloadResult(result: Boolean, imageUrl: String?)
    }

    fun fetchImageFromStorage(itemUrl: String, imageView: ShapeableImageView) {
        Log.d(TAG, "fetchImageFromStorage: $itemUrl")
        val storage = FirebaseStorage.getInstance()
        val gsReference = storage.getReferenceFromUrl(itemUrl)
        gsReference.downloadUrl.addOnSuccessListener { uri ->
            Log.d(TAG, "onSuccessutils: $uri")
            Glide.with(context).load(uri)
                .placeholder(context.getDrawable(R.drawable.ic_baseline_person_24_color))
                .into(imageView)
        }.addOnFailureListener { p0 -> Log.d(TAG, "onFailureutils: $p0") }
    }

    fun putImageIntoStorage(uri: Uri, user_id: String) {
        Log.d(TAG, "saveImageToStorage: $uri")
        storage = Firebase.storage
        var storageRef = storage.reference
        // Create a child reference
        // imagesRef now points to "images"
        var imagesRef: StorageReference? = storageRef.child("profilePictures")
        val imageRef = imagesRef?.child("image_id_$user_id.jpg")
        var uploadTask = uri?.let { imageRef?.putFile(it) }
        uploadTask?.addOnFailureListener {
            Log.d(TAG, "saveImageToStorage: FAILURE: $it")
            resultListener.getUploadResult(false, it.toString())
        }?.addOnSuccessListener { taskSnapshot ->
            Log.d(TAG, "saveImageToStorage: SUCCESS")
            taskSnapshot.storage.downloadUrl.addOnCompleteListener { task ->
                val imageLink = task.result.toString()
                Log.d(TAG, "onComplete: imageLink = $imageLink")
                resultListener.getUploadResult(true, imageLink)
            }
        }
    }
}