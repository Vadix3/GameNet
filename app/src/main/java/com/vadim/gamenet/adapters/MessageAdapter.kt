package com.vadim.gamenet.adapters

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.R
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.Message
import java.text.SimpleDateFormat
import java.util.*


class MessageAdapter(
    context: Context,
    private val dataSet: ArrayList<Message>, myUser: AppUser, mode: Int
) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    object MODE {
        const val SENDER = 0
        const val RECEIVER = 1
    }

    private var context: Context = context
    private val myUser = myUser
    private val mode = mode

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var v: View = view
        var messageLbl: TextView = v.findViewById(R.id.messageRow_LBL_message)
        var timeLbl: TextView = v.findViewById(R.id.messageRow_LBL_time)
        var senderLbl: TextView = v.findViewById(R.id.messageRow_LBL_sender)
        var mainLayout: LinearLayout = v.findViewById(R.id.messageRow_LAY_mainLayout)
        var container: LinearLayout = v.findViewById(R.id.messageRow_LAY_outerContainer)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_message, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d(MyAppClass.Constants.TAG, "onBindViewHolder: Position:$position")
        val temp = dataSet[position]
        if (temp.sender != myUser.username) {
            viewHolder.container.gravity = Gravity.END
            viewHolder.mainLayout.background = context.getDrawable(R.drawable.right_bubble)
            viewHolder.mainLayout.gravity = Gravity.START
        }
        viewHolder.messageLbl.text = temp.messageText
        val date = Date(temp.messageTime)
        val format = SimpleDateFormat("HH:mm")
        viewHolder.timeLbl.text = format.format(date)
        viewHolder.senderLbl.text = temp.sender


    }

    override fun getItemCount() = dataSet.size
}