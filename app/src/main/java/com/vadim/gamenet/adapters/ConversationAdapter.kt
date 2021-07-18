package com.vadim.gamenet.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.fragments.FragmentConversation
import com.vadim.gamenet.interfaces.OpenChatCallback
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.Conversation

class ConversationAdapter(
    context: Context,
    private val dataSet: ArrayList<Conversation>,
    myUser: AppUser,
    mode: Int,
    openChatCallback: OpenChatCallback
) :
    RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    object MODE {
        const val SENDER = 0
        const val RECEIVER = 1
    }

    private var context: Context = context
    private val myUser = myUser
    private val mode = mode
    private val listener = openChatCallback

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var v: View = view
        val rowCard: MaterialCardView = v.findViewById(R.id.conversationRow_LAY_mainCard)
        val imageLbl: ShapeableImageView = v.findViewById(R.id.conversationRow_LBL_image)
        val nameLbl: TextView = v.findViewById(R.id.conversationRow_LBL_name)
        val participantsLbl: TextView = v.findViewById(R.id.conversationRow_LBL_participants)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_conversation, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: Position:$position")
        val temp = dataSet[position]
        //TODO: Add image
        viewHolder.nameLbl.text = temp.name
        viewHolder.participantsLbl.text = getParticipants(temp.participants)
        viewHolder.rowCard.setOnClickListener {
            moveToConversationDialog(temp)
        }
    }


    /**
     * User clicked on a certain conversation.
     * this method will switch a fragment with the selected conversation
     */
    private fun moveToConversationDialog(temp: Conversation) {
        Log.d(TAG, "clicked on conversation")
        val conversationFragment = FragmentConversation(myUser, temp)
        listener.getFragment(conversationFragment,temp.name)
    }

    private fun getParticipants(participants: ArrayList<AppUser>): String {
        Log.d(TAG, "getParticipants: ")
        var str = ""
        for (item in participants) {
            str += item.first_name + ", "
        }
        return str.dropLast(2)
    }

    override fun getItemCount() = dataSet.size
}