package com.vadim.gamenet.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.utils.FirebaseTools


class FragmentSignupUsername(tempUser: AppUser) : Fragment() {

    private val tempUser: AppUser = tempUser
    private lateinit var fbutils: FirebaseTools
    private lateinit var usernameInputBox: TextInputLayout
    private lateinit var usernameInputEdt: TextInputEditText
    private lateinit var continueBtn: MaterialButton
    private lateinit var profileImg: ShapeableImageView
    private lateinit var storage: FirebaseStorage

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    profileImg.setImageURI(data.data)
                    saveImageToStorage(data.data)
                }
            }
        }

    private fun saveImageToStorage(uri: Uri?) {
        Log.d(TAG, "saveImageToStorage: $uri")
        continueBtn.isEnabled = false
        setButtonLoading()
        storage = Firebase.storage
        val storageRef = storage.reference
        val imagesRef: StorageReference? = storageRef.child("profilePictures")
        val imageRef = imagesRef?.child("image_id_${tempUser.email}.jpg")
        val uploadTask = uri?.let { imageRef?.putFile(it) }
        uploadTask?.addOnFailureListener {
            Log.d(TAG, "saveImageToStorage: FAILURE: $it")
        }?.addOnSuccessListener { taskSnapshot ->
            Log.d(TAG, "saveImageToStorage: SUCCESS")
            taskSnapshot.storage.downloadUrl.addOnCompleteListener { task ->
                val imageLink = task.result.toString()
                Log.d(TAG, "onComplete: imageLink = $imageLink")
                tempUser.photo_url = imageLink
                continueBtn.isEnabled = true
            }
        }
    }

    private fun fetchImageFromStorage() {
        Log.d(TAG, "fetchImageFromStorage: ")
        fbutils.fetchImageFromStorage(tempUser.photo_url, profileImg)
    }

    private fun setButtonLoading() {
        Log.d(TAG, "setButtonLoading: ")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

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
        profileImg = mView.findViewById(R.id.newAccountUserName_IMG_profileImage)
        profileImg.setOnClickListener {
            checkForStoragePermissions()
        }
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
            tempUser.username = usernameInputEdt.text.toString()
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
//        transaction.addToBackStack("games_transaction")
        transaction.commit()
    }

    private fun openStorage() {
        Log.d(TAG, "openStorage: ")
        val intent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        resultLauncher.launch(intent)
    }


    private fun checkForStoragePermissions() {
        Log.d(TAG, "checkForStoragePermissions: ")
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    Log.d(TAG, "onPermissionGranted: ")
                    openStorage()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Log.d(TAG, "onPermissionDenied: ")
                    if (p0 != null) {
                        if (p0.isPermanentlyDenied) {
                            Log.d(TAG, "onPermissionDenied: Permanently Denied")
                            val alertBuilder = AlertDialog.Builder(requireActivity())
                            alertBuilder.setTitle(resources.getString(R.string.permission_denied))
                                .setMessage(resources.getString(R.string.allow_storage_permission))
                                .setNegativeButton(resources.getString(R.string.cancel), null)
                                .setPositiveButton(
                                    resources.getString(R.string.ok)
                                ) { _, _ ->
                                    val intent =
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts(
                                                "package",
                                                requireActivity().packageName,
                                                null
                                            )
                                        )
                                    val resultLauncher =
                                        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                                            if (result.resultCode == Activity.RESULT_OK) {
                                                val data: Intent? = result.data
                                                Log.d(
                                                    TAG,
                                                    "onPermissionDenied: got data: ${data?.type}"
                                                )
                                            }
                                        }
                                    resultLauncher.launch(intent)
                                }
                        } else {
                            Log.d(TAG, "onPermissionDenied: User denied permissions!")
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    Log.d(TAG, "onPermissionRationaleShouldBeShown: ")
                    p1?.continuePermissionRequest()
                }
            }).check()
    }
}