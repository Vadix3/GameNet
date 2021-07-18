package com.vadim.gamenet.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.adapters.MessageAdapter
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.Conversation
import com.vadim.gamenet.models.Message
import com.vadim.gamenet.utils.MongoTools
import com.vadim.gamenet.utils.ParsingTools
import org.bson.Document
import org.json.JSONArray
import org.json.JSONObject

class FragmentConversation(myUser: AppUser, conversation: Conversation) : Fragment() {

    private val mongoUser = MyAppClass.Constants.app.currentUser()
    private val myUser = myUser
    private var myConversation = conversation
    private val messagesList = conversation.messages
    private val participants = conversation.participants

    //TODO: participants names and conversation on top

    private lateinit var sendBtn: ShapeableImageView
    private lateinit var messagesRecycler: RecyclerView
    private lateinit var inputLayout: TextInputLayout
    private lateinit var inputEdt: TextInputEditText


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        val mView = inflater.inflate(R.layout.fragment_conversation, container, false)
        initViews(mView)
        populateMessages()
        initMessageWatcher()
        return mView;
    }

    private fun initMessageWatcher() {
        Log.d(TAG, "initMessageWatcher: ")
        val tools = mongoUser?.let {
            MongoTools(requireContext(), it, object : MongoTools.ResultListener {
                override fun getResult(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "getResult watcher: SUCCESS $message")
                        val plainJson = JSONObject(message)
                        val changedId = plainJson.get("id")
                        if (changedId == myConversation.id) {
                            Log.d(TAG, "getResult: my document changed")
                            val temp = ParsingTools.parseConversation(plainJson.toString())
                            myConversation = temp
                            requireActivity().runOnUiThread {
                                refreshMessageList()
                            }
                        } else {
                            Log.d(TAG, "getResult: not my document changed")
                        }
                    } else {
                        Log.d(TAG, "getResult watcher: FAILURE $message")
                    }
                }
            })
        }
        tools?.enableDocumentListener("chat", "conversations", "id", myConversation.id)
    }

    private fun populateMessages() {
        Log.d(TAG, "populateMessages: ")
        val tools = mongoUser?.let {
            MongoTools(requireContext(), it, object : MongoTools.ResultListener {
                override fun getResult(result: Boolean, message: String) {
                    if (result && message != "[]") {
                        Log.d(TAG, "getResult: SUCCESS: $message")
                        val plainJson = JSONArray(message)[0]
                        val temp = ParsingTools.parseConversation(plainJson.toString())
                        myConversation = temp
                        requireActivity().runOnUiThread {
                            refreshMessageList()
                        }
                    } else {
                        Log.d(TAG, "getResult: FAILURE: $message")
                    }
                }
            })
        }

        if (mongoUser != null) {
            tools?.fetchDocumentFromDatabase(
                mongoUser, "chat", "conversations",
                Document("id", myConversation.id)
            )
        }
    }

    private fun refreshMessageList() {
        val adapter =
            MessageAdapter(requireContext(), myConversation.messages, myUser, myConversation.mode)
        messagesRecycler.adapter = adapter
        messagesRecycler.scrollToPosition(myConversation.messages.size - 1)
    }

    private fun initViews(mView: View?) {
        Log.d(TAG, "initViews: ")
        if (mView != null) {
            sendBtn = mView.findViewById(R.id.conversation_BTN_sendBtn)
            messagesRecycler = mView.findViewById(R.id.conversation_LST_messages)
            inputLayout = mView.findViewById(R.id.conversation_EDT_inputLayout)
            inputEdt = mView.findViewById(R.id.conversation_EDT_inputEdt)
            sendBtn.setOnClickListener {
                if (inputEdt.text.toString() != "") {
                    saveMessageToConversation(inputEdt.text.toString())
                    inputEdt.text?.clear()
                }
            }
        }
    }

    private fun saveMessageToConversation(message: String) {
        Log.d(TAG, "saveMessageToConversation: $message")
        val temp = Message()
        temp.sender = myUser.username
        temp.messageText = message
        myConversation.messages.add(temp)
        if (mongoUser != null) {
            val tools = MongoTools(requireContext(), mongoUser, object : MongoTools.ResultListener {
                override fun getResult(result: Boolean, message: String) {
                    if (result) {
                        Log.d(TAG, "getResult: SUCCESS: $message")
                        requireActivity().runOnUiThread {
                            refreshMessageList()
                        }
                    } else {
                        Log.d(TAG, "getResult: FAILURE: $message")
                    }
                }

            })


            val json = Gson().toJson(temp)
            Log.d(TAG, "saveMessageToConversation: $json")
            tools.addItemToArray(
                "chat",
                "conversations",
                "id",
                myConversation.id,
                "messages",
                Gson().toJson(temp)
            )
        }
    }


}