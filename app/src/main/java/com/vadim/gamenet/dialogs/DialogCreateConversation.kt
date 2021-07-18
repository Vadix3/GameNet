package com.vadim.gamenet.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.adapters.CreateConversationAdapter
import com.vadim.gamenet.interfaces.NewConversationDialog
import com.vadim.gamenet.interfaces.SelectedFriendsCallback
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.Conversation
import com.vadim.gamenet.models.Message
import com.vadim.gamenet.utils.MongoTools
import com.vadim.gamenet.utils.ParsingTools
import org.bson.Document
import org.json.JSONArray

class DialogCreateConversation(
    context: Context,
    myUser: AppUser,
    newConversationDialog: NewConversationDialog
) : Dialog(context), SelectedFriendsCallback {

    val myUser = myUser
    val listener = newConversationDialog

    private lateinit var inputLayout: TextInputLayout
    private lateinit var inputEdt: TextInputEditText
    private lateinit var friendsRecycler: RecyclerView
    private lateinit var submitBtn: MaterialButton
    private val friendsList = arrayListOf<AppUser>()
    private lateinit var selectedFriends: BooleanArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_create_conversation)
        initViews()
        fetchUsersFromDB()
    }

    private fun initViews() {
        Log.d(TAG, "initViews: ")
        inputLayout = findViewById(R.id.createConversaion_LBL_inputLayout)
        inputEdt = findViewById(R.id.createConversaion_LBL_inputEdt)
        friendsRecycler = findViewById(R.id.createConversaion_LST_friendsList)
        submitBtn = findViewById(R.id.createConversaion_LST_submitBtn)
        submitBtn.setOnClickListener {
            createNewConversation()
        }
    }

    private fun fetchUsersFromDB() {
        Log.d(TAG, "fetchUsersFromDB: ")
        val mongoUser = MyAppClass.Constants.app.currentUser()
        val myFriends = myUser.friends_list
        if (mongoUser != null) {
            val tools = MongoTools(context, mongoUser, object : MongoTools.ResultListener {
                override fun getResult(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "getResult: SUCCESS: $message")
                        val tempUser = ParsingTools.parseUser(JSONArray(message)[0].toString())
                        friendsList.add(tempUser)
                        refreshFriendsList()
                    } else {
                        Log.d(TAG, "getResult: ERROR: $message")
                    }
                }
            })
            for (tempEmail in myFriends) {
                val query = Document(MongoTools.USER_KEYS.EMAIL, tempEmail)
                tools.fetchDocumentFromDatabase(mongoUser, "gamenet_users", "custom_data", query)
            }
        }
    }

    private fun refreshFriendsList() {
        Log.d(TAG, "refreshFriendsList: ")
        friendsRecycler.adapter = CreateConversationAdapter(context, friendsList, myUser, this)
    }

    private fun createNewConversation() {
        Log.d(TAG, "createNewConversation: ")
        val selectedFriends = getSelectedFriends()
        val myConversation = Conversation()
        myConversation.participants = selectedFriends
        myConversation.messages = arrayListOf()
        if (selectedFriends.size > 1) {
            myConversation.mode = Conversation.MODE.GROUP
        } else {
            myConversation.mode = Conversation.MODE.PRIVATE
        }
        if (inputEdt.text.toString() == "") {
            Toast.makeText(context, "Please enter group name", Toast.LENGTH_SHORT).show()
        } else {
            myConversation.name = inputEdt.text.toString()
        }
        listener.getConversation(myConversation)
        dismiss()
    }

    private fun getSelectedFriends(): ArrayList<AppUser> {
        Log.d(TAG, "getSelectedFriends: ")
        val list = arrayListOf<AppUser>()
        for (i in 0 until friendsList.size) {
            if (selectedFriends[i]) {
                list.add(friendsList[i])
                Log.d(TAG, "getSelectedFriends: selected: ${friendsList[i].email}")
            }
        }
        return list
    }

    override fun getSelectedItems(selectedList: BooleanArray) {
        Log.d(TAG, "getSelectedItems: $selectedList")
        selectedFriends = selectedList
    }
}