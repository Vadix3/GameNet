package com.vadim.gamenet.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.adapters.ConversationAdapter
import com.vadim.gamenet.dialogs.DialogCreateConversation
import com.vadim.gamenet.interfaces.NewConversationDialog
import com.vadim.gamenet.interfaces.OpenChatCallback
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.Conversation
import com.vadim.gamenet.utils.MongoTools
import com.vadim.gamenet.utils.ParsingTools
import org.bson.Document
import org.json.JSONArray

class FragmentChat(myUser: AppUser, myContext: Context) : Fragment(), NewConversationDialog {

    val mongoUser = MyAppClass.Constants.app.currentUser()
    val myContext = myContext
    private val myUser = myUser
    private val conversationList = arrayListOf<Conversation>()
    private lateinit var createConversationBtn: FloatingActionButton
    private lateinit var conversationRecycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        val mView = inflater.inflate(R.layout.fragment_chat, container, false)
        initViews(mView)
        fetchConversations()
        return mView;
    }

    /** A method to fetch all the conversations of the current user*/
    private fun fetchConversations() {
        Log.d(TAG, "fetchConversations: ")
        if (mongoUser != null) {
            val tools = MongoTools(requireContext(), mongoUser, object : MongoTools.ResultListener {
                override fun getResult(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "getResult conversations: SUCCESS: $message")
                        val plainJsonArray = JSONArray(message)
                        for (i in 0 until plainJsonArray.length()) {
                            val tempConversation =
                                ParsingTools.parseConversation(plainJsonArray.get(i).toString())
                            conversationList.add(tempConversation)
                            requireActivity().runOnUiThread {
                                refreshConversationList()
                            }
                        }
                    } else {
                        Log.d(TAG, "getResult: FAILURE: $message")
                    }
                }
            })
            val listOfConversations = myUser.list_of_conversations
            Log.d(TAG, "fetchConversations: my conversations: $listOfConversations")
            for (item in listOfConversations) {
                tools.fetchDocumentFromDatabase(
                    mongoUser, "chat", "conversations",
                    Document("id", item)
                )
            }
        }
    }

    private fun initViews(mView: View?) {
        Log.d(TAG, "initViews: ")
        if (mView != null) {
            createConversationBtn = mView.findViewById(R.id.chat_BTN_addConversation)
            createConversationBtn.setOnClickListener {
                addConversation()
            }
            conversationRecycler = mView.findViewById(R.id.chat_LST_conversationList)
        }
    }

    private fun addConversation() {
        Log.d(TAG, "addConversation: ")


        val dialog =
            DialogCreateConversation(requireContext(), myUser, this as NewConversationDialog)
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val width = (requireContext().resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window!!.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setDimAmount(0.9f)
    }

    override fun getConversation(conversation: Conversation) {
        Log.d(TAG, "getConversation: $conversation")
        val tools = mongoUser?.let {
            MongoTools(requireContext(), it, object : MongoTools.ResultListener {
                override fun getResult(result: Boolean, message: kotlin.String) {
                    if (result) {
                        Log.d(TAG, "getResult: SUCCESS: $message")
                        updateUsersConversations(conversation)
                        conversationList.add(conversation)
                        refreshConversationList()
                    } else {
                        Log.d(TAG, "getResult: FAILURE: $message")
                        Toast.makeText(
                            requireContext(),
                            "Error creating conversation",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        }
        if (mongoUser != null) {
            tools?.addDocumentToDatabase(
                mongoUser,
                "chat",
                "conversations",
                MongoTools.TYPE.CONVERSATION,
                Gson().toJson(conversation)
            )
        }
    }

    private fun getParticipants(conversation: Conversation): ArrayList<String> {
        val emailList = arrayListOf<String>()
        val participantList = conversation.participants
        for (item in participantList) {
            emailList.add(item.email)
        }
        Log.d(TAG, "getParticipants: email list: $emailList")
        return emailList
    }

    private fun updateUsersConversations(conversation: Conversation) {
        Log.d(TAG, "updateUsersConversations: adding: $id")
        val participantEmails = getParticipants(conversation)
        participantEmails.add(myUser.email)
        val tools = mongoUser?.let {
            MongoTools(requireContext(), it, object : MongoTools.ResultListener {
                override fun getResult(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "getResult: SUCCESS: $message")
                        // here I get the user in an array
                        val userJson = JSONArray(message).get(0)
                        val tempUser = ParsingTools.parseUser(userJson.toString())
                        Log.d(TAG, "getResult: pasred user: $tempUser")
                        tempUser.list_of_conversations.add(conversation.id)
                        val tools2 = MongoTools(
                            requireContext(),
                            mongoUser,
                            object : MongoTools.ResultListener {
                                override fun getResult(result: Boolean, message: String) {
                                    if (result) {
                                        Log.d(TAG, "getResult: SUCCESS: $message")

                                    } else {
                                        Log.d(TAG, "getResult: FAILURE: $message")
                                    }
                                }

                            })
                        tools2.saveUserCustomData(
                            MongoTools.USER_KEYS.LIST_OF_CONVERSATIONS,
                            Gson().toJson(tempUser.list_of_conversations),
                            tempUser.email
                        )
                    } else {
                        Log.d(TAG, "getResult: FAILURE: $message")
                    }
                }
            })
        }
        if (mongoUser != null) {

            for (item in participantEmails)
                tools?.fetchDocumentFromDatabase(
                    mongoUser,
                    "gamenet_users",
                    "custom_data",
                    Document(MongoTools.USER_KEYS.EMAIL, item)
                )
        }

    }


    private fun refreshConversationList() {
        Log.d(TAG, "refreshFriendsList: ")
        val adapter = ConversationAdapter(
            requireContext(),
            conversationList,
            myUser,
            ConversationAdapter.MODE.SENDER,
            context as OpenChatCallback
        )
        //Not sure if mode is even needed
        conversationRecycler.adapter = adapter
    }
}